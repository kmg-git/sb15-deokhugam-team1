package com.example.sb15deokhugamteam1.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularBookTest {

  @Test
  @DisplayName("빌더로 PopularBook 생성 시 전달한 랭킹 및 도서 정보가 정상 반영된다")
  void build_popularBook_setsFields() {
    // given: 인기 도서 생성 입력값 준비
    UUID bookId = UUID.randomUUID();
    LocalDate baseDate = LocalDate.of(2026, 11, 24);

    // when: PopularBook 엔티티 생성
    PopularBook popularBook = PopularBook.builder()
        .bookId(bookId)
        .title("클린 코드")
        .author("로버트 C. 마틴")
        .thumbnailUrl("https://example.com/clean-code.png")
        .period(Period.DAILY)
        .baseDate(baseDate)
        .rank(1L)
        .score(92.5)
        .reviewCount(15L)
        .rating(4.8)
        .build();

    // then: 주요 필드 값 검증
    assertThat(popularBook.getBookId()).isEqualTo(bookId);
    assertThat(popularBook.getTitle()).isEqualTo("클린 코드");
    assertThat(popularBook.getAuthor()).isEqualTo("로버트 C. 마틴");
    assertThat(popularBook.getThumbnailUrl()).isEqualTo("https://example.com/clean-code.png");
    assertThat(popularBook.getPeriod()).isEqualTo(Period.DAILY);
    assertThat(popularBook.getBaseDate()).isEqualTo(baseDate);
    assertThat(popularBook.getRank()).isEqualTo(1L);
    assertThat(popularBook.getScore()).isEqualTo(92.5);
    assertThat(popularBook.getReviewCount()).isEqualTo(15L);
    assertThat(popularBook.getRating()).isEqualTo(4.8);
  }
}