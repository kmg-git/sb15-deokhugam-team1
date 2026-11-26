package com.example.sb15deokhugamteam1.exception;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;
@Getter
public abstract class BaseException extends RuntimeException {
    private final Instant timestamp;
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    protected BaseException(ErrorCode errorCode, Map<String, Object> details){
        this.timestamp = Instant.now();
        this.errorCode = errorCode;
        this.details = details;
    }

}
