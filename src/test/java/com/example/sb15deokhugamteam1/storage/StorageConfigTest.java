package com.example.sb15deokhugamteam1.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class StorageConfigTest {

    @TempDir
    Path dir;

    @Test
    void 버킷이_없으면_로컬_폴더_보관소() {
        StorageProperties properties = new StorageProperties(new StorageProperties.S3("", null), dir.toString());

        assertThat(new StorageConfig().imageStorage(properties)).isInstanceOf(LocalImageStorage.class);
    }

    @Test
    void 버킷이_있으면_S3_보관소() {
        StorageProperties properties = new StorageProperties(
                new StorageProperties.S3("deokhugam-thumbnails", "ap-northeast-2"), dir.toString());

        assertThat(new StorageConfig().imageStorage(properties)).isInstanceOf(S3ImageStorage.class);
    }

    @Test
    void S3_요청에는_시간_제한이_걸려_있다() {
        try (S3Client client = StorageConfig.s3Client(new StorageProperties.S3("deokhugam-thumbnails", null))) {
            ClientOverrideConfiguration config = client.serviceClientConfiguration().overrideConfiguration();

            assertThat(config.apiCallAttemptTimeout()).contains(Duration.ofSeconds(5));
            assertThat(config.apiCallTimeout()).contains(Duration.ofSeconds(10));
        }
    }

    @Test
    void 설정이_비어_있으면_기본값을_쓴다() {
        StorageProperties properties = new StorageProperties(null, null);

        assertThat(properties.s3().enabled()).isFalse();
        assertThat(properties.s3().region()).isEqualTo("ap-northeast-2");
        assertThat(properties.localDir()).isEqualTo("uploads");
    }
}
