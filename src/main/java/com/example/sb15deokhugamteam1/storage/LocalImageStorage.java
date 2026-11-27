package com.example.sb15deokhugamteam1.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 로컬 폴더 저장 (S3 버킷 미설정 시 쓰는 개발용)
 * 저장 파일은 LocalFileWebConfig 가 /files/** 주소로 제공
 */
@Slf4j
public class LocalImageStorage implements ImageStorage {

    public static final String URL_PREFIX = "/files/";

    private final Path root;

    public LocalImageStorage(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public String upload(String key, byte[] bytes, String contentType) {
        Path target = resolve(key);
        if (target == null) {
            throw new ImageUploadException(key, new IllegalArgumentException("잘못된 경로"));
        }
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new ImageUploadException(key, e);
        }
        return URL_PREFIX + key;
    }

    @Override
    public void delete(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }
        Path target = resolve(url.substring(URL_PREFIX.length()));
        if (target == null) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("로컬 이미지 삭제 실패: {}", target, e);
        }
    }

    /** ../ 등으로 저장 폴더 밖 접근 차단. 밖이면 null */
    private Path resolve(String key) {
        Path target = root.resolve(key).normalize();
        return target.startsWith(root) && !target.equals(root) ? target : null;
    }

    static void requireWritable(Path root) {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("이미지 저장 폴더를 만들 수 없습니다: " + root, e);
        }
    }
}
