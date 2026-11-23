package com.example.sb15deokhugamteam1.exception.book;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** ISBN-13 형식(978/979로 시작하는 13자리 숫자)이 아닐 때 → 400 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidIsbnException extends RuntimeException {

    public InvalidIsbnException(String isbn) {
        super("ISBN 형식이 올바르지 않습니다. isbn=" + isbn);
    }
}
