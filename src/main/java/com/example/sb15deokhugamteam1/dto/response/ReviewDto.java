package com.example.sb15deokhugamteam1.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
    UUID id,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String content,
    Integer rating,
    Integer likeCount,
    Integer commentCount,
    Boolean likedByMe,
    Instant createdAt,
    Instant updatedAt
) {
}
