package com.example.sb15deokhugamteam1.exception.comment;

import java.util.Map;
import java.util.UUID;

public class CommentNotFoundException extends CommentException {
    public CommentNotFoundException(UUID commentId) {

        super(CommentErrorCode.COMMENT_NOT_FOUND, Map.of("commentId",commentId));
    }
}
