package com.example.sb15deokhugamteam1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 도서. 스키마 원본은 schema.sql 의 books 테이블.
 *
 * <p>리뷰 집계(reviewCount, ratingSum, rating)는 updatable = false 로 막아 두었다.
 * 엔티티 변경 감지로 덮어쓰지 않고, BookRepository 의 원자적 UPDATE 로만 바꾼다.
 */
@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseUpdatableEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String publisher;

    @Column(nullable = false)
    private LocalDate publishedDate;

    @Column(length = 13)
    private String isbn;

    private String thumbnailUrl;

    @Column(nullable = false, updatable = false)
    private int reviewCount;

    @Column(nullable = false, updatable = false)
    private long ratingSum;

    @Column(nullable = false, updatable = false)
    private double rating;

    private Instant deletedAt;

    public Book(String title, String author, String description, String publisher,
                LocalDate publishedDate, String isbn, String thumbnailUrl) {
        this.title = requireText(title, "title");
        this.author = requireText(author, "author");
        this.description = requireText(description, "description");
        this.publisher = requireText(publisher, "publisher");
        this.publishedDate = requireNonNull(publishedDate, "publishedDate");
        this.isbn = isbn;
        this.thumbnailUrl = thumbnailUrl;
    }

    /** PATCH 의미: null 로 들어온 값은 바꾸지 않는다. ISBN 은 수정 대상이 아니다. */
    public void update(String title, String author, String description,
                       String publisher, LocalDate publishedDate) {
        if (title != null) this.title = requireText(title, "title");
        if (author != null) this.author = requireText(author, "author");
        if (description != null) this.description = requireText(description, "description");
        if (publisher != null) this.publisher = requireText(publisher, "publisher");
        if (publishedDate != null) this.publishedDate = publishedDate;
    }

    public void changeThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /** 논리 삭제. 이미 삭제된 책이면 처음 삭제 시각을 유지한다. */
    public void softDelete(Instant now) {
        if (this.deletedAt == null) {
            this.deletedAt = requireNonNull(now, "now");
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        return value;
    }
}
