package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookCreateRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 255) String author,
    @NotBlank String description,
    @NotBlank @Size(max = 255) String publisher,
    @NotNull LocalDate publishedDate,
    String isbn
) {
}
