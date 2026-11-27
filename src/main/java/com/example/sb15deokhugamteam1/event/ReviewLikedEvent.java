package com.example.sb15deokhugamteam1.event;

import java.util.UUID;

public record ReviewLikedEvent(
        UUID reviewId,
        UUID reviewAuthorId,
        UUID likerId,
        String likerNickname
) {
}
