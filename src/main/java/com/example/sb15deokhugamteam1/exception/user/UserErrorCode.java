package com.example.sb15deokhugamteam1.exception.user;

import com.example.sb15deokhugamteam1.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {
    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT,"USER_01","이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_02", "사용자를 찾을 수 없습니다."),
    USER_FORBIDDEN(HttpStatus.FORBIDDEN, "USER_03", "본인만 수정, 삭제할 수 있습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED,"LOGIN_01","이메일 또는 비밀번호가 올바르지 않습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    UserErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
