package com.example.sb15deokhugamteam1.exception.review;

import com.example.sb15deokhugamteam1.exception.BaseException;

import java.util.Map;

public abstract class ReviewException extends BaseException {

    protected ReviewException(ReviewErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
