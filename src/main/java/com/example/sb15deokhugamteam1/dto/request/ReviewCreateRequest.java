package com.example.sb15deokhugamteam1.dto.request;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReviewCreateRequest {

    @NotNull
    private UUID bookId;

    @NotBlank
    @Size(max = 5000)
    private String content;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;


}
