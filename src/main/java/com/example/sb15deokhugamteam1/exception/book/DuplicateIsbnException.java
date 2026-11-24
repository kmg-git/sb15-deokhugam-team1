package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;

/** 삭제되지 않은 도서 중에 같은 ISBN이 이미 있을 때 → 409 */
public class DuplicateIsbnException extends BookException {

    public DuplicateIsbnException(String isbn) {
        super(BookErrorCode.DUPLICATE_ISBN, Map.of("isbn", isbn));
    }
}
