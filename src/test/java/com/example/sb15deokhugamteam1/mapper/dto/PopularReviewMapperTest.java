package com.example.sb15deokhugamteam1.mapper.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sb15deokhugamteam1.dto.response.PopularReviewDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularReview;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PopularReviewMapperTest {

  private final PopularReviewMapper mapper = new PopularReviewMapper();

  @Test
  @DisplayName("toEntity 호출 시 전달한 값으로 PopularReview 엔티티를 생성한다")
  void toEntity_mapsFieldsCorrectly() {
    // given 리뷰·랭킹 입력값 준비
    UUID reviewId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    LocalDate baseDate = LocalDate.of(2026, 11, 24);

    // when toEntity 호출
    PopularReview entity = mapper.toEntity(
        reviewId,
        bookId,
        "도서 제목",
        "https://example.com/thumb.png",
        userId,
        "리뷰어",
        "리뷰 내용",
        4.5,
        Period.DAILY,
        baseDate,
        1L,
        77.5,
        15L,
        8L
    );

    // then 엔티티 주요 필드 매핑 검증
    assertThat(entity.getReviewId()).isEqualTo(reviewId);
    assertThat(entity.getBookId()).isEqualTo(bookId);
    assertThat(entity.getBookTitle()).isEqualTo("도서 제목");
    assertThat(entity.getBookThumbnailUrl()).isEqualTo("https://example.com/thumb.png");
    assertThat(entity.getUserId()).isEqualTo(userId);
    assertThat(entity.getUserNickname()).isEqualTo("리뷰어");
    assertThat(entity.getReviewContent()).isEqualTo("리뷰 내용");
    assertThat(entity.getReviewRating()).isEqualTo(4.5);
    assertThat(entity.getPeriod()).isEqualTo(Period.DAILY);
    assertThat(entity.getBaseDate()).isEqualTo(baseDate);
    assertThat(entity.getRank()).isEqualTo(1L);
    assertThat(entity.getScore()).isEqualTo(77.5);
    assertThat(entity.getLikeCount()).isEqualTo(15L);
    assertThat(entity.getCommentCount()).isEqualTo(8L);
  }

  @Test
  @DisplayName("toDto 호출 시 PopularReview 엔티티를 PopularReviewDto로 변환한다")
  void toDto_mapsEntityToDto() {
    // given id와 createdAt이 세팅된 PopularReview 엔티티 준비
    UUID id = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-11-24T01:00:00Z");
    LocalDate baseDate = LocalDate.of(2026, 11, 24);

    PopularReview entity = mapper.toEntity(
        reviewId,
        bookId,
        "도서 제목",
        "https://example.com/thumb.png",
        userId,
        "리뷰어",
        "리뷰 내용",
        4.5,
        Period.MONTHLY,
        baseDate,
        3L,
        65.0,
        10L,
        5L
    );
    ReflectionTestUtils.setField(entity, "id", id);
    ReflectionTestUtils.setField(entity, "createdAt", createdAt);

    // when toDto 호출
    PopularReviewDto dto = mapper.toDto(entity);

    // then DTO 필드 매핑 검증
    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(id);
    assertThat(dto.reviewId()).isEqualTo(reviewId);
    assertThat(dto.bookId()).isEqualTo(bookId);
    assertThat(dto.bookTitle()).isEqualTo("도서 제목");
    assertThat(dto.bookThumbnailUrl()).isEqualTo("https://example.com/thumb.png");
    assertThat(dto.userId()).isEqualTo(userId);
    assertThat(dto.userNickname()).isEqualTo("리뷰어");
    assertThat(dto.reviewContent()).isEqualTo("리뷰 내용");
    assertThat(dto.reviewRating()).isEqualTo(4.5);
    assertThat(dto.period()).isEqualTo(Period.MONTHLY);
    assertThat(dto.createdAt()).isEqualTo(createdAt);
    assertThat(dto.rank()).isEqualTo(3L);
    assertThat(dto.score()).isEqualTo(65.0);
    assertThat(dto.likeCount()).isEqualTo(10L);
    assertThat(dto.commentCount()).isEqualTo(5L);
  }

  @Test
  @DisplayName("toDto에 null이 전달되면 null을 반환한다")
  void toDto_whenNull_returnsNull() {
    // given when null 입력으로 toDto 호출
    PopularReviewDto dto = mapper.toDto(null);

    // then null 반환 검증
    assertThat(dto).isNull();
  }
}