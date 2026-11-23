package com.example.sb15deokhugamteam1.mapper.dto;

import com.example.sb15deokhugamteam1.dto.request.response.ReviewDto;
import com.example.sb15deokhugamteam1.entity.Review;

public class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewDto toDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getBook().getId(), // book entity 구현 후 연결
                review.getUser().getId(), // user entity 구현 후 연결
                review.getContent(),
                review.getRating(),
                review.getLikeCount(),
                review.getCommentCount(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
