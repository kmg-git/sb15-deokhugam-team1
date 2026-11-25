package com.example.sb15deokhugamteam1.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ImageSignatureTest {

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }

    @Test
    void 지원하는_이미지_형식은_통과한다() {
        assertThat(ImageSignature.isSupported(bytes(0xFF, 0xD8, 0xFF, 0xE0))).isTrue();          // JPEG
        assertThat(ImageSignature.isSupported(bytes(0x89, 'P', 'N', 'G', 0x0D))).isTrue();      // PNG
        assertThat(ImageSignature.isSupported("GIF89a".getBytes(StandardCharsets.US_ASCII))).isTrue();
        assertThat(ImageSignature.isSupported("BM....".getBytes(StandardCharsets.US_ASCII))).isTrue();
        assertThat(ImageSignature.isSupported(bytes('I', 'I', 0x2A, 0x00))).isTrue();          // TIFF
        assertThat(ImageSignature.isSupported(bytes('M', 'M', 0x00, 0x2A))).isTrue();          // TIFF
        assertThat(ImageSignature.isSupported("RIFF0000WEBPVP8 ".getBytes(StandardCharsets.US_ASCII))).isTrue();
    }

    @Test
    void 이미지가_아니면_막는다() {
        assertThat(ImageSignature.isSupported(new byte[0])).isFalse();
        assertThat(ImageSignature.isSupported(bytes(0xFF))).isFalse();
        assertThat(ImageSignature.isSupported("plain text".getBytes(StandardCharsets.UTF_8))).isFalse();
        assertThat(ImageSignature.isSupported("%PDF-1.7".getBytes(StandardCharsets.US_ASCII))).isFalse();
        assertThat(ImageSignature.isSupported("RIFF0000WAVEfmt ".getBytes(StandardCharsets.US_ASCII))).isFalse();
    }
}
