package com.example.sb15deokhugamteam1.exception.book;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/** 없거나 논리 삭제된 도서를 찾을 때 → 404 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(UUID bookId) {
        super("도서를 찾을 수 없습니다. bookId=" + bookId);
    }
}
