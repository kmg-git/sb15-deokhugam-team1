package com.example.sb15deokhugamteam1.dto.response;

import com.example.sb15deokhugamteam1.entity.Period;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PopularReviewDto(
    UUID id,
    UUID reviewId,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String reviewContent,
    Double reviewRating,
    Period period,
    OffsetDateTime createdAt,
    Long rank,
    Double score,
    Long likeCount,
    Long commentCount
) {
}
