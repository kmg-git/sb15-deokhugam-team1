package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(
    @Email
    @NotBlank
    String email,

    @NotNull String password
) {
}
