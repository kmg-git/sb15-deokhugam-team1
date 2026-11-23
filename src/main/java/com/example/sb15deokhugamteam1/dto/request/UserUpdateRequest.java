package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotNull @Size(min = 2, max = 20) String nickname
) {
}
