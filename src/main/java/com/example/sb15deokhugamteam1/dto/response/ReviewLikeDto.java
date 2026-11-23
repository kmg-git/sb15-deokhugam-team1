package com.example.sb15deokhugamteam1.dto.response;

import java.util.UUID;

public record ReviewLikeDto(
    UUID reviewId,
    UUID userId,
    Boolean liked
) {
}
