package com.example.sb15deokhugamteam1.event;

import java.util.UUID;

public record CommentCreatedEvent(
        UUID reviewId,
        UUID reviewAuthorId,
        UUID commenterId,
        String commenterNickname
) {
}
