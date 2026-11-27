package com.example.sb15deokhugamteam1.exception.review;

import com.example.sb15deokhugamteam1.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "REVIEW_NOT_FOUND",
            "리뷰를 찾을 수 없습니다."
    ),

    REVIEW_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "REVIEW_FORBIDDEN",
            "리뷰를 삭제할 권한이 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ReviewErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}