package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
    @NotNull String email,
    @NotNull @Size(min = 2, max = 20) String nickname,
    @NotNull @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$") String password
) {
}
