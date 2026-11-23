package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewUpdateRequest(
    @NotNull String content,
    @NotNull @Min(1) @Max(5) Integer rating
) {
}
