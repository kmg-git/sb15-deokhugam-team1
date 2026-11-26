package com.example.sb15deokhugamteam1.exception.comment;

import java.util.Map;
import java.util.UUID;

public class CommentNotEqualUserException extends CommentException {
    public CommentNotEqualUserException(UUID userId) {
        super(CommentErrorCode.NOT_EQUAL_USER, Map.of("userId",userId));
    }
}
