package com.example.sb15deokhugamteam1.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;
import java.time.Duration;

/** S3_BUCKET 환경변수 있으면 S3, 없으면 로컬 폴더 보관소 생성 */
@Slf4j
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    static final Duration ATTEMPT_TIMEOUT = Duration.ofSeconds(5);
    static final Duration CALL_TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public ImageStorage imageStorage(StorageProperties properties) {
        StorageProperties.S3 s3 = properties.s3();
        if (s3.enabled()) {
            log.info("이미지 보관소: S3 (bucket={}, region={})", s3.bucket(), s3.region());
            return new S3ImageStorage(s3Client(s3), s3.bucket(), s3.region());
        }
        Path root = Path.of(properties.localDir());
        LocalImageStorage.requireWritable(root);
        log.info("이미지 보관소: 로컬 폴더 ({}) — S3_BUCKET 을 넣으면 S3 로 바뀝니다", root.toAbsolutePath());
        return new LocalImageStorage(root);
    }

    /**
     * S3 지연 시 사용자 장시간 대기 방지용 시간 제한
     * - 시도당 최대 5초, 재시도 포함 최대 10초
     * - 초과 시 502(BOOK_11)
     */
    static S3Client s3Client(StorageProperties.S3 s3) {
        return S3Client.builder()
                .region(Region.of(s3.region()))
                .overrideConfiguration(c -> c
                        .apiCallAttemptTimeout(ATTEMPT_TIMEOUT)
                        .apiCallTimeout(CALL_TIMEOUT))
                .build();
    }
}
