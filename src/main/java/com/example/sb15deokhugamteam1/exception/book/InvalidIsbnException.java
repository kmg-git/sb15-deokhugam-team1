package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;

/** ISBN-13 형식(978/979로 시작하는 13자리 숫자)이 아닐 때 → 400 */
public class InvalidIsbnException extends BookException {

    public InvalidIsbnException(String isbn) {
        super(BookErrorCode.INVALID_ISBN, Map.of("isbn", isbn));
    }
}
