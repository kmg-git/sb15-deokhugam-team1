package com.example.sb15deokhugamteam1.dto.request.response;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ReviewDto {

    private final UUID id;
    private final UUID bookId;
    private final UUID userId;
    private final String content;
    private final Integer rating;
    private final int likeCount;
    private final int commentCount;
    private final Instant createdAt;
    private final Instant updatedAt;

    public ReviewDto(
            UUID id,
            UUID bookId,
            UUID userId,
            String content,
            Integer rating,
            int likeCount,
            int commentCount,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.content = content;
        this.rating = rating;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }




}
