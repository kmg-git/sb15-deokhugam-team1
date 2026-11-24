package com.example.sb15deokhugamteam1.dto.request;

import com.example.sb15deokhugamteam1.exception.book.InvalidSortFieldException;

import java.util.Arrays;

/** 도서 목록 정렬 기준. 쿼리 파라미터 orderBy 값과 엔티티 필드 이름이 같다. */
public enum BookSortField {
    TITLE("title"),
    PUBLISHED_DATE("publishedDate"),
    RATING("rating"),
    REVIEW_COUNT("reviewCount");

    private final String field;

    BookSortField(String field) {
        this.field = field;
    }

    public String field() {
        return field;
    }

    /** null 이나 빈 값이면 기본값(title), 모르는 값이면 400 */
    public static BookSortField from(String value) {
        if (value == null || value.isBlank()) {
            return TITLE;
        }
        return Arrays.stream(values())
                .filter(f -> f.field.equals(value))
                .findFirst()
                .orElseThrow(() -> new InvalidSortFieldException(value));
    }
}
