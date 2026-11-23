package com.example.sb15deokhugamteam1.mapper.dto;

import com.example.sb15deokhugamteam1.dto.response.ReviewDto;
import com.example.sb15deokhugamteam1.entity.Review;


public class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewDto toDto(Review review) {
        return toDto(review, false);
    }

    public static ReviewDto toDto(Review review, boolean likedByMe) {
        return new ReviewDto(
                review.getId(),

                // 도서 정보
                review.getBook().getId(),
                review.getBook().getTitle(),
                review.getBook().getThumbnailUrl(),

                // 작성자 정보
                review.getUser().getId(),
                review.getUser().getNickname(),

                // 리뷰 정보
                review.getContent(),
                review.getRating(),
                review.getLikeCount(),
                review.getCommentCount(),
                likedByMe,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
