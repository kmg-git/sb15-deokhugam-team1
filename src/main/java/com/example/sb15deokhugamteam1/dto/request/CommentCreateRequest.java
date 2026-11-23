package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentCreateRequest(
    @NotNull UUID reviewId,
    @NotNull UUID userId,
    @NotNull String content
) {
}
