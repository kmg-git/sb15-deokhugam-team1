package com.example.sb15deokhugamteam1.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

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
    void 설정이_비어_있으면_기본값을_쓴다() {
        StorageProperties properties = new StorageProperties(null, null);

        assertThat(properties.s3().enabled()).isFalse();
        assertThat(properties.s3().region()).isEqualTo("ap-northeast-2");
        assertThat(properties.localDir()).isEqualTo("uploads");
    }
}
