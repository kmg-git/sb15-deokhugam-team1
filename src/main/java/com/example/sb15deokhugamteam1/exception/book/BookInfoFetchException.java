package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;

/** 카카오 API 호출 자체가 실패했을 때 (키 없음, 인증 실패, 타임아웃 등) → 502 */
public class BookInfoFetchException extends BookException {

    public BookInfoFetchException(String isbn) {
        super(BookErrorCode.BOOK_INFO_FETCH_FAILED, Map.of("isbn", isbn));
    }
}
