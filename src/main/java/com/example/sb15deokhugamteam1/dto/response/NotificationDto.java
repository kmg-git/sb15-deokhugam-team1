package com.example.sb15deokhugamteam1.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID userId,
    UUID reviewId,
    String reviewContent,
    String message,
    Boolean confirmed,
    Instant createdAt,
    Instant updatedAt
) {
}
