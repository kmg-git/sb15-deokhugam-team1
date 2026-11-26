package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;

/** 카카오에서 해당 ISBN 의 도서 정보를 찾지 못했을 때 → 404 */
public class BookInfoNotFoundException extends BookException {

    public BookInfoNotFoundException(String isbn) {
        super(BookErrorCode.BOOK_INFO_NOT_FOUND, Map.of("isbn", isbn));
    }
}
