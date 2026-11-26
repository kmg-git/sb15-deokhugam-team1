package com.example.sb15deokhugamteam1.exception.comment;

import com.example.sb15deokhugamteam1.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommentErrorCode implements ErrorCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"COMMENT_01", "댓글을 찾을 수 없습니다."),
    NOT_EQUAL_USER(HttpStatus.FORBIDDEN, "COMMENT_02", "본인만 수정, 삭제할 수 있습니다.")

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    CommentErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
