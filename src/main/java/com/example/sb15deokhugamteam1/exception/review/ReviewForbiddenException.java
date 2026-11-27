package com.example.sb15deokhugamteam1.exception.review;

import java.util.Map;
import java.util.UUID;

public class ReviewForbiddenException extends ReviewException {

    public ReviewForbiddenException(UUID reviewId) {
        super(
                ReviewErrorCode.REVIEW_FORBIDDEN,
                Map.of("reviewId", reviewId)
        );
    }
}