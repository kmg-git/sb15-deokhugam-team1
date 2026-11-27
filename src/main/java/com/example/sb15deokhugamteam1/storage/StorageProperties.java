package com.example.sb15deokhugamteam1.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.yaml 의 storage.* 설정 */
@ConfigurationProperties(prefix = "storage")
public record StorageProperties(S3 s3, String localDir) {

    public StorageProperties {
        if (s3 == null) {
            s3 = new S3(null, null);
        }
        if (localDir == null || localDir.isBlank()) {
            localDir = "uploads";
        }
    }

    public record S3(String bucket, String region) {

        public S3 {
            if (region == null || region.isBlank()) {
                region = "ap-northeast-2";
            }
        }

        public boolean enabled() {
            return bucket != null && !bucket.isBlank();
        }
    }
}
