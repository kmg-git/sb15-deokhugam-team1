package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.exception.book.InvalidImageException;
import com.example.sb15deokhugamteam1.exception.book.ThumbnailUploadException;
import com.example.sb15deokhugamteam1.storage.ImageStorage;
import com.example.sb15deokhugamteam1.storage.ImageUploadException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookThumbnailServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A};

    @Mock
    ImageStorage imageStorage;

    @InjectMocks
    BookThumbnailService service;

    @AfterEach
    void clearTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private static MockMultipartFile file(byte[] bytes) {
        return new MockMultipartFile("thumbnailImage", "cover.png", "image/png", bytes);
    }

    @Test
    void 이미지가_없거나_비어_있으면_아무것도_안_하고_null() {
        assertThat(service.store(null)).isNull();
        assertThat(service.store(file(new byte[0]))).isNull();
        verify(imageStorage, never()).upload(anyString(), any(), anyString());
    }

    @Test
    void PNG는_books_경로에_png로_올리고_주소를_돌려준다() {
        given(imageStorage.upload(matches("books/[0-9a-f-]{36}\\.png"), eq(PNG), eq("image/png")))
                .willReturn("https://bucket/books/x.png");

        assertThat(service.store(file(PNG))).isEqualTo("https://bucket/books/x.png");
    }

    @Test
    void 이미지가_아니면_InvalidImageException이고_올리지_않는다() {
        MockMultipartFile text = file("hello".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.store(text)).isInstanceOf(InvalidImageException.class);
        verify(imageStorage, never()).upload(anyString(), any(), anyString());
    }

    @Test
    void 보관소_저장이_실패하면_ThumbnailUploadException() {
        given(imageStorage.upload(anyString(), any(), anyString()))
                .willThrow(new ImageUploadException("books/x.png", new RuntimeException("down")));

        assertThatThrownBy(() -> service.store(file(PNG))).isInstanceOf(ThumbnailUploadException.class);
    }

    @Test
    void 트랜잭션이_롤백되면_새로_올린_파일을_지운다() {
        TransactionSynchronizationManager.initSynchronization();
        given(imageStorage.upload(anyString(), any(), anyString())).willReturn("https://bucket/books/x.png");

        service.store(file(PNG));
        completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(imageStorage).delete("https://bucket/books/x.png");
    }

    @Test
    void 트랜잭션이_커밋되면_새로_올린_파일은_남긴다() {
        TransactionSynchronizationManager.initSynchronization();
        given(imageStorage.upload(anyString(), any(), anyString())).willReturn("https://bucket/books/x.png");

        service.store(file(PNG));
        completeTransaction(TransactionSynchronization.STATUS_COMMITTED);

        verify(imageStorage, never()).delete(any());
    }

    @Test
    void 예전_파일은_커밋된_뒤에만_지운다() {
        TransactionSynchronizationManager.initSynchronization();

        service.deleteAfterCommit("https://bucket/books/old.png");
        verify(imageStorage, never()).delete(any());

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(imageStorage).delete("https://bucket/books/old.png");
    }

    @Test
    void 트랜잭션_밖에서는_바로_지우고_null은_무시한다() {
        service.deleteAfterCommit(null);
        verify(imageStorage, never()).delete(any());

        service.deleteAfterCommit("https://bucket/books/old.png");
        verify(imageStorage).delete("https://bucket/books/old.png");
    }

    private static void completeTransaction(int status) {
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(status));
    }
}
