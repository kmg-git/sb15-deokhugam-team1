package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentUpdateRequest(
    @NotBlank String content
) {
}
