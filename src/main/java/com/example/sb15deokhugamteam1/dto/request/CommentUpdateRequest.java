package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.NotNull;

public record CommentUpdateRequest(
    @NotNull String content
) {
}
