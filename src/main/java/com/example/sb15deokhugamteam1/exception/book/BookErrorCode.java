package com.example.sb15deokhugamteam1.exception.book;

import com.example.sb15deokhugamteam1.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BookErrorCode implements ErrorCode {

    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_01", "도서를 찾을 수 없습니다."),
    DUPLICATE_ISBN(HttpStatus.CONFLICT, "BOOK_02", "이미 등록된 ISBN입니다."),
    INVALID_ISBN(HttpStatus.BAD_REQUEST, "BOOK_03", "ISBN 형식이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    BookErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
