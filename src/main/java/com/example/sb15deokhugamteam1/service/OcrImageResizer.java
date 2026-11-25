package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.exception.book.InvalidImageException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * OCR.space 무료 플랜은 1MB 까지만 받으므로, 큰 사진은 JPEG 로 줄여서 보낸다
 * 이미 작은 이미지는 손대지 않는다
 */
final class OcrImageResizer {

    /** OCR.space 제한(1024KB)보다 살짝 작게 잡는다 */
    static final int MAX_BYTES = 1_000_000;

    /** 긴 변 기준 시작 크기. 바코드 숫자를 읽기에 충분하다 */
    private static final int START_MAX_SIDE = 2000;
    private static final int MIN_SIDE = 500;

    private OcrImageResizer() {
    }

    /** 결과 이미지와 OCR.space 에 알려줄 파일 이름 */
    record Result(byte[] bytes, String filename) {
    }

    static Result fit(byte[] original, String filename) {
        if (original.length <= MAX_BYTES) {
            return new Result(original, filename);
        }

        BufferedImage image = read(original, filename);
        int maxSide = Math.min(START_MAX_SIDE, Math.max(image.getWidth(), image.getHeight()));
        byte[] jpeg = toJpeg(scale(image, maxSide));
        // 그래도 크면 25%씩 더 줄인다
        while (jpeg.length > MAX_BYTES && maxSide > MIN_SIDE) {
            maxSide = Math.max(MIN_SIDE, maxSide * 3 / 4);
            jpeg = toJpeg(scale(image, maxSide));
        }
        if (jpeg.length > MAX_BYTES) {
            throw new InvalidImageException(filename);
        }
        return new Result(jpeg, "image.jpg");
    }

    private static BufferedImage read(byte[] bytes, String filename) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                // 자바가 못 읽는 형식 (예: 아이폰 HEIC)
                throw new InvalidImageException(filename);
            }
            return image;
        } catch (IOException e) {
            throw new InvalidImageException(filename);
        }
    }

    /** 긴 변이 maxSide 가 되도록 비율을 유지하며 줄인다. 투명 배경은 흰색으로 채운다 */
    private static BufferedImage scale(BufferedImage source, int maxSide) {
        double ratio = Math.min(1.0, (double) maxSide / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));

        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return target;
    }

    private static byte[] toJpeg(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.85f);
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }
}
