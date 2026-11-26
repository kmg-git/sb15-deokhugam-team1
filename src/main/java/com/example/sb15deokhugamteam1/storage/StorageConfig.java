package com.example.sb15deokhugamteam1.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;

/** S3_BUCKET 환경변수가 있으면 S3, 없으면 로컬 폴더 보관소를 만든다 */
@Slf4j
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    public ImageStorage imageStorage(StorageProperties properties) {
        StorageProperties.S3 s3 = properties.s3();
        if (s3.enabled()) {
            log.info("이미지 보관소: S3 (bucket={}, region={})", s3.bucket(), s3.region());
            S3Client client = S3Client.builder()
                    .region(Region.of(s3.region()))
                    .build();
            return new S3ImageStorage(client, s3.bucket(), s3.region());
        }
        Path root = Path.of(properties.localDir());
        LocalImageStorage.requireWritable(root);
        log.info("이미지 보관소: 로컬 폴더 ({}) — S3_BUCKET 을 넣으면 S3 로 바뀝니다", root.toAbsolutePath());
        return new LocalImageStorage(root);
    }
}
