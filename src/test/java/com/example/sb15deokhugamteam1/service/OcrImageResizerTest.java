package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.exception.book.InvalidImageException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OcrImageResizerTest {

    @Test
    void 작은_이미지는_그대로_보낸다() {
        byte[] small = {1, 2, 3};

        OcrImageResizer.Result result = OcrImageResizer.fit(small, "book.png");

        assertThat(result.bytes()).isSameAs(small);
        assertThat(result.filename()).isEqualTo("book.png");
    }

    @Test
    void 큰_이미지는_1MB_이하_JPEG로_줄인다() throws IOException {
        byte[] big = noisePng(1500, 1500);
        assertThat(big.length).isGreaterThan(OcrImageResizer.MAX_BYTES);

        OcrImageResizer.Result result = OcrImageResizer.fit(big, "book.png");

        assertThat(result.bytes().length).isLessThanOrEqualTo(OcrImageResizer.MAX_BYTES);
        assertThat(result.filename()).isEqualTo("image.jpg");
        BufferedImage resized = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertThat(resized).isNotNull();
        assertThat(resized.getWidth()).isEqualTo(resized.getHeight());
    }

    @Test
    void 크면서_읽을_수_없는_파일이면_InvalidImageException() {
        byte[] notImage = new byte[OcrImageResizer.MAX_BYTES + 1];

        assertThatThrownBy(() -> OcrImageResizer.fit(notImage, "book.heic"))
                .isInstanceOf(InvalidImageException.class);
    }

    /** 압축이 잘 안 되는 무작위 점 이미지 → PNG 가 1MB 를 넘는다 */
    private static byte[] noisePng(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(42);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, random.nextInt(0xFFFFFF));
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
