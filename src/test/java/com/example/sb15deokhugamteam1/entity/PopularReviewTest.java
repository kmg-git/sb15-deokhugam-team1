package com.example.sb15deokhugamteam1.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularReviewTest {

  @Test
  @DisplayName("빌더로 PopularReview 생성 시 전달한 값이 정상 반영된다")
  void create_popularReview_withBuilder() {
    // given: 인기 리뷰 생성용 입력값 준비
    UUID reviewId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    LocalDate baseDate = LocalDate.of(2026, 11, 24);

    // when: 빌더로 PopularReview 생성
    PopularReview popularReview = PopularReview.builder()
        .reviewId(reviewId)
        .bookId(bookId)
        .bookTitle("오브젝트")
        .bookThumbnailUrl("https://example.com/object.png")
        .userId(userId)
        .userNickname("독서왕")
        .reviewContent("객체지향 설계를 다시 보게 만든 책입니다.")
        .reviewRating(5.0)
        .period(Period.WEEKLY)
        .baseDate(baseDate)
        .rank(1L)
        .score(88.0)
        .likeCount(24L)
        .commentCount(9L)
        .build();

    // then: 필드 값 검증
    assertThat(popularReview.getReviewId()).isEqualTo(reviewId);
    assertThat(popularReview.getBookId()).isEqualTo(bookId);
    assertThat(popularReview.getBookTitle()).isEqualTo("오브젝트");
    assertThat(popularReview.getBookThumbnailUrl()).isEqualTo("https://example.com/object.png");
    assertThat(popularReview.getUserId()).isEqualTo(userId);
    assertThat(popularReview.getUserNickname()).isEqualTo("독서왕");
    assertThat(popularReview.getReviewContent()).isEqualTo("객체지향 설계를 다시 보게 만든 책입니다.");
    assertThat(popularReview.getReviewRating()).isEqualTo(5.0);
    assertThat(popularReview.getPeriod()).isEqualTo(Period.WEEKLY);
    assertThat(popularReview.getBaseDate()).isEqualTo(baseDate);
    assertThat(popularReview.getRank()).isEqualTo(1L);
    assertThat(popularReview.getScore()).isEqualTo(88.0);
    assertThat(popularReview.getLikeCount()).isEqualTo(24L);
    assertThat(popularReview.getCommentCount()).isEqualTo(9L);
  }
}