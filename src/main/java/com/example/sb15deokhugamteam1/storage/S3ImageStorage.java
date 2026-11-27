package com.example.sb15deokhugamteam1.storage;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * AWS S3 저장
 * - 키는 코드에 없음. AWS SDK 가 환경변수(AWS_ACCESS_KEY_ID 등) 또는 ECS 작업 역할에서 자동 조회
 * - 버킷 books/ 경로 공개 읽기 필요 (브라우저에서 주소로 바로 표지 열람)
 */
@Slf4j
public class S3ImageStorage implements ImageStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String baseUrl;

    public S3ImageStorage(S3Client s3Client, String bucket, String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.baseUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
    }

    @Override
    public String upload(String key, byte[] bytes, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromBytes(bytes));
        } catch (SdkException e) {
            throw new ImageUploadException(key, e);
        }
        return baseUrl + key;
    }

    /** 삭제는 정리 작업 → 실패해도 요청은 성공 처리, 로그만 남김 */
    @Override
    public void delete(String url) {
        if (url == null || !url.startsWith(baseUrl)) {
            return;
        }
        String key = url.substring(baseUrl.length());
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (SdkException e) {
            log.warn("S3 이미지 삭제 실패: key={}", key, e);
        }
    }
}
