package com.example.sb15deokhugamteam1.exception.book;

import com.example.sb15deokhugamteam1.exception.BaseException;

import java.util.Map;

public abstract class BookException extends BaseException {

    protected BookException(BookErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
