package com.example.sb15deokhugamteam1.dto.response;

public record BookInfoDto(
    String title,
    String author,
    String description,
    String publisher,
    String publishedDate,
    String isbn,
    byte[] thumbnailImage
) {
}
