package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;

/** 빈 파일이거나 읽을 수 없는 이미지일 때 → 400 */
public class InvalidImageException extends BookException {

    public InvalidImageException(String filename) {
        super(BookErrorCode.INVALID_IMAGE, Map.of("filename", filename == null ? "" : filename));
    }
}
