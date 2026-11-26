package com.example.sb15deokhugamteam1.mapper.dto;

import com.example.sb15deokhugamteam1.dto.response.BookDto;
import com.example.sb15deokhugamteam1.entity.Book;

public class BookMapper {

    private BookMapper() {
    }

    public static BookDto toDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getIsbn(),
                book.getThumbnailUrl(),
                book.getReviewCount(),
                book.getRating(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
