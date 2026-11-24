package com.example.sb15deokhugamteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponsePopularReviewDto;
import com.example.sb15deokhugamteam1.dto.response.PopularReviewDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularReview;
import com.example.sb15deokhugamteam1.mapper.dto.PopularReviewMapper;
import com.example.sb15deokhugamteam1.repository.PopularReviewRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PopularReviewQueryServiceTest {

  @Mock
  private PopularReviewRepository popularReviewRepository;

  @Mock
  private PopularReviewMapper popularReviewMapper;

  @InjectMocks
  private PopularReviewQueryService popularReviewQueryService;

  @Test
  @DisplayName("다음 페이지가 있으면 hasNext=true와 nextCursor, nextAfter를 반환한다")
  void getPopularReviews_whenHasNext_returnsNextCursorAndAfter() {
    // given
    Period period = Period.DAILY;
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    int size = 1;

    PopularReview first = createPopularReview(period, baseDate, 1L, 50.0);
    PopularReview second = createPopularReview(period, baseDate, 2L, 40.0);
    Instant firstCreatedAt = Instant.parse("2026-11-24T01:00:00Z");
    ReflectionTestUtils.setField(first, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(first, "createdAt", firstCreatedAt);

    PopularReviewDto firstDto = new PopularReviewDto(
        first.getId(), first.getReviewId(), first.getBookId(), first.getBookTitle(),
        first.getBookThumbnailUrl(), first.getUserId(), first.getUserNickname(),
        first.getReviewContent(), first.getReviewRating(), first.getPeriod(),
        first.getCreatedAt(), first.getRank(), first.getScore(),
        first.getLikeCount(), first.getCommentCount()
    );

    given(popularReviewRepository.findRankingWithCursor(period, baseDate, null, PageRequest.of(0, size + 1)))
        .willReturn(List.of(first, second));
    given(popularReviewMapper.toDto(first)).willReturn(firstDto);

    // when
    CursorPageResponsePopularReviewDto response = popularReviewQueryService.getPopularReviews(
        period, baseDate, null, size
    );

    // then
    assertThat(response.content()).hasSize(1);
    assertThat(response.hasNext()).isTrue();
    assertThat(response.nextCursor()).isEqualTo("1");
    assertThat(response.nextAfter()).isEqualTo(firstCreatedAt);
  }

  @Test
  @DisplayName("데이터가 없으면 빈 목록과 hasNext=false를 반환한다")
  void getPopularReviews_whenEmpty_returnsEmptyPage() {
    // given
    Period period = Period.WEEKLY;
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    int size = 10;

    given(popularReviewRepository.findRankingWithCursor(period, baseDate, null, PageRequest.of(0, size + 1)))
        .willReturn(List.of());

    // when
    CursorPageResponsePopularReviewDto response = popularReviewQueryService.getPopularReviews(
        period, baseDate, null, size
    );

    // then
    assertThat(response.content()).isEmpty();
    assertThat(response.hasNext()).isFalse();
    assertThat(response.nextCursor()).isNull();
    assertThat(response.nextAfter()).isNull();
  }

  private PopularReview createPopularReview(
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score
  ) {
    return PopularReview.builder()
        .reviewId(UUID.randomUUID())
        .bookId(UUID.randomUUID())
        .bookTitle("도서 제목")
        .bookThumbnailUrl("https://example.com/book.png")
        .userId(UUID.randomUUID())
        .userNickname("작성자")
        .reviewContent("리뷰 내용")
        .reviewRating(4.5)
        .period(period)
        .baseDate(baseDate)
        .rank(rank)
        .score(score)
        .likeCount(10L)
        .commentCount(5L)
        .build();
  }
}