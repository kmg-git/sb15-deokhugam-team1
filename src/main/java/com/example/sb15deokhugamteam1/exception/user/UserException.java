package com.example.sb15deokhugamteam1.exception.user;

import com.example.sb15deokhugamteam1.exception.BaseException;

import java.util.Map;

public abstract class UserException extends BaseException {

    protected UserException(UserErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
