package com.example.sb15deokhugamteam1.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalImageStorageTest {

    @TempDir
    Path root;

    @Test
    void 폴더에_저장하고_files_주소를_돌려준다() throws IOException {
        LocalImageStorage storage = new LocalImageStorage(root);

        String url = storage.upload("books/a.png", new byte[]{1, 2, 3}, "image/png");

        assertThat(url).isEqualTo("/files/books/a.png");
        assertThat(Files.readAllBytes(root.resolve("books/a.png"))).containsExactly(1, 2, 3);
    }

    @Test
    void 자기_주소인_파일만_지운다() throws IOException {
        LocalImageStorage storage = new LocalImageStorage(root);
        String url = storage.upload("books/a.png", new byte[]{1}, "image/png");

        storage.delete("https://elsewhere.com/books/a.png");
        storage.delete(null);
        assertThat(root.resolve("books/a.png")).exists();

        storage.delete(url);
        assertThat(root.resolve("books/a.png")).doesNotExist();
    }

    @Test
    void 저장_폴더_밖으로_나가는_경로는_막는다() throws IOException {
        Path outside = Files.writeString(root.resolveSibling(root.getFileName() + "-secret.txt"), "secret");
        LocalImageStorage storage = new LocalImageStorage(root);

        assertThatThrownBy(() -> storage.upload("../x.png", new byte[]{1}, "image/png"))
                .isInstanceOf(ImageUploadException.class);
        storage.delete("/files/../" + outside.getFileName());

        assertThat(outside).exists();
        Files.delete(outside);
    }
}
