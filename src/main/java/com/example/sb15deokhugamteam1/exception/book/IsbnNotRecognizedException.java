package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;

/** 이미지에서 올바른 ISBN 을 찾지 못했을 때 → 400 */
public class IsbnNotRecognizedException extends BookException {

    public IsbnNotRecognizedException() {
        super(BookErrorCode.ISBN_NOT_RECOGNIZED, Map.of());
    }
}
