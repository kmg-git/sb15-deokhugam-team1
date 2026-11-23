package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;
import java.util.UUID;

/** 없거나 논리 삭제된 도서를 찾을 때 → 404 */
public class BookNotFoundException extends BookException {

    public BookNotFoundException(UUID bookId) {
        super(BookErrorCode.BOOK_NOT_FOUND, Map.of("bookId", bookId));
    }
}
