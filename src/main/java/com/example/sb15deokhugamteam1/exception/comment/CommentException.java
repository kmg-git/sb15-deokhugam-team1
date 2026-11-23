package com.example.sb15deokhugamteam1.exception.comment;

import com.example.sb15deokhugamteam1.exception.BaseException;

import java.util.Map;

public abstract class CommentException extends BaseException {

    protected CommentException(CommentErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
