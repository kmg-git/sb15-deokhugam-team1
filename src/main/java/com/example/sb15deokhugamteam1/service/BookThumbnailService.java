package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.exception.book.InvalidImageException;
import com.example.sb15deokhugamteam1.exception.book.ThumbnailUploadException;
import com.example.sb15deokhugamteam1.storage.ImageStorage;
import com.example.sb15deokhugamteam1.storage.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 도서 표지 이미지 저장·삭제.
 *
 * <p>파일 저장(S3)과 DB 저장은 따로라서, 둘 중 하나만 성공하면 안 된다.
 * 그래서 DB 트랜잭션 결과를 보고 파일을 정리한다.
 * <ul>
 *   <li>새로 올린 파일: DB 가 롤백되면 지운다 (주인 없는 파일 방지)</li>
 *   <li>예전 파일: DB 커밋이 끝난 뒤에 지운다 (롤백됐는데 파일만 사라지는 것 방지)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class BookThumbnailService {

    private static final String KEY_PREFIX = "books/";

    private final ImageStorage imageStorage;

    /** 이미지가 없거나 비어 있으면 null. 있으면 검사 후 저장하고 주소를 돌려준다 */
    public String store(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        String filename = image.getOriginalFilename() == null ? "thumbnail" : image.getOriginalFilename();

        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (IOException e) {
            throw new InvalidImageException(filename);
        }
        String extension = ImageSignature.webImageExtension(bytes)
                .orElseThrow(() -> new InvalidImageException(filename));

        String url;
        try {
            url = imageStorage.upload(KEY_PREFIX + UUID.randomUUID() + "." + extension,
                    bytes, contentTypeOf(extension));
        } catch (ImageUploadException e) {
            throw new ThumbnailUploadException(e);
        }
        deleteOnRollback(url);
        return url;
    }

    /** 지금 트랜잭션이 커밋된 뒤에 지운다. 트랜잭션 밖이면 바로 지운다 */
    public void deleteAfterCommit(String url) {
        if (url == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            imageStorage.delete(url);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                imageStorage.delete(url);
            }
        });
    }

    private void deleteOnRollback(String url) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    imageStorage.delete(url);
                }
            }
        });
    }

    private static String contentTypeOf(String extension) {
        return switch (extension) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
