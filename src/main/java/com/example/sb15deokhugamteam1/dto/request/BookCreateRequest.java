package com.example.sb15deokhugamteam1.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookCreateRequest(
    @NotNull String title,
    @NotNull String author,
    @NotNull String description,
    @NotNull String publisher,
    @NotNull LocalDate publishedDate,
    String isbn
) {
}
