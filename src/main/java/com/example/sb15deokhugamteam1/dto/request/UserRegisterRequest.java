package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.*;

public record UserRegisterRequest(
    @Email
    @NotBlank
    String email,
    @NotBlank @Size(min = 2, max = 20) String nickname,
    @NotNull @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$") String password
) {
}
