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
 * 도서 표지 이미지 저장·삭제
 *
 * <p>파일 저장(S3)과 DB 저장이 별개 → DB 트랜잭션 결과 기준으로 파일 정리
 * <ul>
 *   <li>새로 올린 파일: DB 롤백 시 삭제 (주인 없는 파일 방지)</li>
 *   <li>예전 파일: DB 커밋 후 삭제 (롤백인데 파일만 사라짐 방지)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class BookThumbnailService {

    private static final String KEY_PREFIX = "books/";

    private final ImageStorage imageStorage;

    /** 이미지 없음·빈 파일 → null. 있으면 검사 후 저장, 주소 반환 */
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

    /** 트랜잭션 커밋 후 삭제. 트랜잭션 밖이면 즉시 삭제 */
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
