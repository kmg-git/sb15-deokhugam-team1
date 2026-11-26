package com.example.sb15deokhugamteam1.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookTest {

    private static final LocalDate PUBLISHED = LocalDate.of(2013, 12, 24);

    private static Book book() {
        return new Book("클린 코드", "로버트 C. 마틴", "설명", "인사이트", PUBLISHED, "9788966260959", null);
    }

    @Test
    void 새_도서는_리뷰_수와_평점이_0이고_삭제되지_않은_상태다() {
        Book book = book();

        assertThat(book.getReviewCount()).isZero();
        assertThat(book.getRating()).isZero();
        assertThat(book.isDeleted()).isFalse();
    }

    @Test
    void 제목이_비어_있으면_만들_수_없다() {
        assertThatThrownBy(() -> new Book(" ", "저자", "설명", "출판사", PUBLISHED, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
    }

    @Test
    void 출판일이_없으면_만들_수_없다() {
        assertThatThrownBy(() -> new Book("제목", "저자", "설명", "출판사", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("publishedDate");
    }

    @Test
    void 수정할_때_null_로_온_값은_그대로_둔다() {
        Book book = book();

        book.update("클린 코드 2판", null, null, null, null);

        assertThat(book.getTitle()).isEqualTo("클린 코드 2판");
        assertThat(book.getAuthor()).isEqualTo("로버트 C. 마틴");
        assertThat(book.getPublishedDate()).isEqualTo(PUBLISHED);
    }

    @Test
    void 빈_값으로는_수정할_수_없다() {
        Book book = book();

        assertThatThrownBy(() -> book.update(null, "  ", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("author");
    }

    @Test
    void 표지_주소를_바꿀_수_있다() {
        Book book = book();

        book.changeThumbnailUrl("https://bucket/cover.png");

        assertThat(book.getThumbnailUrl()).isEqualTo("https://bucket/cover.png");
    }

    @Test
    void 두_번_삭제해도_처음_삭제_시각을_유지한다() {
        Book book = book();
        Instant first = Instant.parse("2026-11-01T00:00:00Z");

        book.softDelete(first);
        book.softDelete(Instant.parse("2026-11-02T00:00:00Z"));

        assertThat(book.isDeleted()).isTrue();
        assertThat(book.getDeletedAt()).isEqualTo(first);
    }
}
