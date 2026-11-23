package com.example.sb15deokhugamteam1.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {


    //valid
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_01", "입력값이 올바르지 않습니다."),

    //상정되지 않은 오류
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_99", "서버에 문제가 발생했습니다.")
    ;



    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code,  String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
