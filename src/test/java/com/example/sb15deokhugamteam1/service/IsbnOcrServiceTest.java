package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.client.ocr.OcrSpaceClient;
import com.example.sb15deokhugamteam1.exception.book.InvalidImageException;
import com.example.sb15deokhugamteam1.exception.book.IsbnNotRecognizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IsbnOcrServiceTest {

    @Mock
    OcrSpaceClient ocrSpaceClient;

    @InjectMocks
    IsbnOcrService isbnOcrService;

    /** JPEG 시그니처로 시작하는 가짜 이미지 */
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01};

    private static MockMultipartFile image(String contentType, byte[] bytes) {
        return new MockMultipartFile("image", "book.jpg", contentType, bytes);
    }

    @Test
    void 이미지에서_읽은_글자_속_ISBN을_돌려준다() {
        byte[] bytes = JPEG;
        given(ocrSpaceClient.parseText(bytes, "book.jpg")).willReturn("클린 코드\nISBN 978-89-6626-095-9");

        String isbn = isbnOcrService.recognize(image("image/jpeg", bytes));

        assertThat(isbn).isEqualTo("9788966260959");
    }

    @Test
    void 파일_이름이_없으면_image_jpg로_보낸다() {
        byte[] bytes = JPEG;
        given(ocrSpaceClient.parseText(bytes, "image.jpg")).willReturn("9788966260959");

        String isbn = isbnOcrService.recognize(new MockMultipartFile("image", "", "image/jpeg", bytes));

        assertThat(isbn).isEqualTo("9788966260959");
    }

    @Test
    void ISBN이_안_보이면_IsbnNotRecognizedException() {
        given(ocrSpaceClient.parseText(any(), anyString())).willReturn("글자는 있지만 ISBN 은 없음");

        assertThatThrownBy(() -> isbnOcrService.recognize(image("image/png", JPEG)))
                .isInstanceOf(IsbnNotRecognizedException.class);
    }

    @Test
    void 빈_파일이면_OCR을_부르지_않고_InvalidImageException() {
        assertThatThrownBy(() -> isbnOcrService.recognize(image("image/jpeg", new byte[0])))
                .isInstanceOf(InvalidImageException.class);
        verify(ocrSpaceClient, never()).parseText(any(), anyString());
    }

    @Test
    void 이미지가_아닌_파일이면_InvalidImageException() {
        assertThatThrownBy(() -> isbnOcrService.recognize(image("application/pdf", JPEG)))
                .isInstanceOf(InvalidImageException.class);
        verify(ocrSpaceClient, never()).parseText(any(), anyString());
    }

    @Test
    void 이미지라고_적혀_있어도_내용이_이미지가_아니면_InvalidImageException() {
        byte[] text = "plain text".getBytes();

        assertThatThrownBy(() -> isbnOcrService.recognize(image("image/png", text)))
                .isInstanceOf(InvalidImageException.class);
        verify(ocrSpaceClient, never()).parseText(any(), anyString());
    }
}
