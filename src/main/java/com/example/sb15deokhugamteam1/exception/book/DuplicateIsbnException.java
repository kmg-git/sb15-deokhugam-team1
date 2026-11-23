package com.example.sb15deokhugamteam1.exception.book;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 삭제되지 않은 도서 중에 같은 ISBN이 이미 있을 때 → 409 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException(String isbn) {
        super("이미 등록된 ISBN입니다. isbn=" + isbn);
    }
}
