package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.client.ocr.OcrSpaceClient;
import com.example.sb15deokhugamteam1.exception.book.InvalidImageException;
import com.example.sb15deokhugamteam1.exception.book.IsbnNotRecognizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** 도서 사진 → OCR 글자 → ISBN-13 */
@Service
@RequiredArgsConstructor
public class IsbnOcrService {

    private final OcrSpaceClient ocrSpaceClient;

    public String recognize(MultipartFile image) {
        String filename = image.getOriginalFilename() == null || image.getOriginalFilename().isBlank()
                ? "image.jpg" : image.getOriginalFilename();
        if (image.isEmpty() || !isImage(image.getContentType())) {
            throw new InvalidImageException(filename);
        }

        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (IOException e) {
            throw new InvalidImageException(filename);
        }
        if (!ImageSignature.isSupported(bytes)) {
            throw new InvalidImageException(filename);
        }

        OcrImageResizer.Result fitted = OcrImageResizer.fit(bytes, filename);
        String text = ocrSpaceClient.parseText(fitted.bytes(), fitted.filename());
        return IsbnExtractor.extract(text).orElseThrow(IsbnNotRecognizedException::new);
    }

    private static boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}
