package com.example.sb15deokhugamteam1.exception.book;

import com.example.sb15deokhugamteam1.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BookErrorCode implements ErrorCode {
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    BookErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
