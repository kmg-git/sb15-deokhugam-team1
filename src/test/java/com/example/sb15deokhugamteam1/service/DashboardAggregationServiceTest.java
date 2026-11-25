package com.example.sb15deokhugamteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularBook;
import com.example.sb15deokhugamteam1.entity.PopularReview;
import com.example.sb15deokhugamteam1.entity.PowerUser;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.PopularBookRaw;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.PopularReviewRaw;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.UserCommentCountRaw;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.UserReviewActivityRaw;
import com.example.sb15deokhugamteam1.repository.PopularBookRepository;
import com.example.sb15deokhugamteam1.repository.PopularReviewRepository;
import com.example.sb15deokhugamteam1.repository.PowerUserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DashboardAggregationService 단위 테스트.
 * 점수 가중치 계산, 기간별 구간 산출, 동점자 정렬 기준, 기존 스냅샷 삭제 후 저장 흐름을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class DashboardAggregationServiceTest {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  @Mock
  private DashboardAggregationRepository aggregationRepository;

  @Mock
  private PopularBookRepository popularBookRepository;

  @Mock
  private PopularReviewRepository popularReviewRepository;

  @Mock
  private PowerUserRepository powerUserRepository;

  @InjectMocks
  private DashboardAggregationService service;

  private PopularBookRaw bookRaw(
      UUID bookId,
      String title,
      long reviewCount,
      double avgRating,
      Instant createdAt
  ) {
    return new PopularBookRaw() {
      @Override
      public UUID getBookId() {
        return bookId;
      }

      @Override
      public String getTitle() {
        return title;
      }

      @Override
      public String getAuthor() {
        return "작가";
      }

      @Override
      public String getThumbnailUrl() {
        return "https://example.com/thumb.png";
      }

      @Override
      public Long getReviewCount() {
        return reviewCount;
      }

      @Override
      public Double getAverageRating() {
        return avgRating;
      }

      @Override
      public Instant getBookCreatedAt() {
        return createdAt;
      }
    };
  }

  private PopularReviewRaw reviewRaw(
      UUID reviewId,
      UUID bookId,
      UUID userId,
      String nickname,
      long likeCount,
      long periodCommentCount,
      Instant createdAt
  ) {
    return new PopularReviewRaw() {
      @Override
      public UUID getReviewId() {
        return reviewId;
      }

      @Override
      public UUID getBookId() {
        return bookId;
      }

      @Override
      public String getBookTitle() {
        return "도서 제목";
      }

      @Override
      public String getBookThumbnailUrl() {
        return "https://example.com/book.png";
      }

      @Override
      public UUID getUserId() {
        return userId;
      }

      @Override
      public String getUserNickname() {
        return nickname;
      }

      @Override
      public String getReviewContent() {
        return "리뷰 내용";
      }

      @Override
      public Double getReviewRating() {
        return 5.0;
      }

      @Override
      public Long getLikeCount() {
        return likeCount;
      }

      @Override
      public Long getPeriodCommentCount() {
        return periodCommentCount;
      }

      @Override
      public Instant getReviewCreatedAt() {
        return createdAt;
      }
    };
  }

  private UserReviewActivityRaw userReviewRaw(
      UUID userId,
      String nickname,
      Instant userCreatedAt,
      UUID reviewId,
      long reviewLikeCount,
      long reviewPeriodCommentCount
  ) {
    return new UserReviewActivityRaw() {
      @Override
      public UUID getUserId() {
        return userId;
      }

      @Override
      public String getNickname() {
        return nickname;
      }

      @Override
      public Instant getUserCreatedAt() {
        return userCreatedAt;
      }

      @Override
      public UUID getReviewId() {
        return reviewId;
      }

      @Override
      public Long getReviewLikeCount() {
        return reviewLikeCount;
      }

      @Override
      public Long getReviewPeriodCommentCount() {
        return reviewPeriodCommentCount;
      }
    };
  }

  private UserCommentCountRaw userCommentRaw(
      UUID userId,
      String nickname,
      Instant userCreatedAt,
      long commentCount
  ) {
    return new UserCommentCountRaw() {
      @Override
      public UUID getUserId() {
        return userId;
      }

      @Override
      public String getNickname() {
        return nickname;
      }

      @Override
      public Instant getUserCreatedAt() {
        return userCreatedAt;
      }

      @Override
      public Long getCommentCount() {
        return commentCount;
      }
    };
  }

  // [인기 도서 집계]
  // 점수 공식: (리뷰 수 * 0.4) + (평균 평점 * 0.6)
  // 저장 순서: 기존 스냅샷 삭제 -> 점수 내림차순 순위 부여 -> 새 스냅샷 일괄 저장
  @Test
  @DisplayName("인기 도서 점수와 순위를 계산하고 기존 스냅샷 삭제 후 저장한다")
  void aggregatePopularBooks_calculatesScoreAndSavesSnapshot() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 25);
    Period period = Period.DAILY;
    Instant start = baseDate.minusDays(1).atStartOfDay(KST).toInstant();
    Instant end = baseDate.atStartOfDay(KST).toInstant();

    UUID lowBookId = UUID.randomUUID();
    UUID highBookId = UUID.randomUUID();

    // lowBook: (1 * 0.4) + (4.0 * 0.6) = 2.8
    // highBook: (5 * 0.4) + (4.5 * 0.6) = 4.7
    when(aggregationRepository.findPopularBookRaw(start, end))
        .thenReturn(List.of(
            bookRaw(lowBookId, "낮은책", 1L, 4.0, Instant.parse("2026-11-20T00:00:00Z")),
            bookRaw(highBookId, "높은책", 5L, 4.5, Instant.parse("2026-11-21T00:00:00Z"))
        ));

    // when
    service.aggregatePopularBooks(period, baseDate, start, end);

    // then
    InOrder order = inOrder(popularBookRepository);
    order.verify(popularBookRepository).deleteAllByPeriodAndBaseDate(period, baseDate);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PopularBook>> captor = ArgumentCaptor.forClass(List.class);
    order.verify(popularBookRepository).saveAll(captor.capture());

    List<PopularBook> saved = captor.getValue();
    assertThat(saved).hasSize(2);
    assertThat(saved.get(0).getBookId()).isEqualTo(highBookId);
    assertThat(saved.get(0).getRank()).isEqualTo(1L);
    assertThat(saved.get(0).getScore()).isCloseTo(4.7, within(1e-6));

    assertThat(saved.get(1).getBookId()).isEqualTo(lowBookId);
    assertThat(saved.get(1).getRank()).isEqualTo(2L);
    assertThat(saved.get(1).getScore()).isCloseTo(2.8, within(1e-6));
  }

  // [인기 도서 동점 정렬]
  // 점수가 동일할 때 리뷰 수 내림차순 -> 생성일 오름차순으로 순위를 결정
  @Test
  @DisplayName("인기 도서 점수가 같으면 리뷰 수와 생성일 기준으로 정렬한다")
  void aggregatePopularBooks_breaksTieByReviewCountAndCreatedAt() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 25);
    Period period = Period.DAILY;
    Instant start = baseDate.minusDays(1).atStartOfDay(KST).toInstant();
    Instant end = baseDate.atStartOfDay(KST).toInstant();

    UUID fewerReviewsBookId = UUID.randomUUID();
    UUID laterSameReviewsBookId = UUID.randomUUID();
    UUID earlierSameReviewsBookId = UUID.randomUUID();

    // 모두 (3 * 0.4) + (3.0 * 0.6) = 3.0점으로 동일
    // 리뷰 3개 vs 0개 비교는 (3*0.4 + 2.0*0.6 = 2.4) vs (0*0.4 + 4.0*0.6 = 2.4)로 구성
    UUID bookWithReviews = UUID.randomUUID();
    UUID bookWithoutReviews = UUID.randomUUID();

    when(aggregationRepository.findPopularBookRaw(start, end))
        .thenReturn(List.of(
            bookRaw(bookWithoutReviews, "평점만높은책", 0L, 4.0, Instant.parse("2026-11-20T00:00:00Z")),
            bookRaw(bookWithReviews, "리뷰많은책", 3L, 2.0, Instant.parse("2026-11-22T00:00:00Z")),
            bookRaw(laterSameReviewsBookId, "나중생성책", 0L, 4.0, Instant.parse("2026-11-23T00:00:00Z")),
            bookRaw(earlierSameReviewsBookId, "먼저생성책", 0L, 4.0, Instant.parse("2026-11-19T00:00:00Z"))
        ));

    // when
    service.aggregatePopularBooks(period, baseDate, start, end);

    // then
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PopularBook>> captor = ArgumentCaptor.forClass(List.class);
    verify(popularBookRepository).saveAll(captor.capture());

    List<PopularBook> saved = captor.getValue();
    assertThat(saved).hasSize(4);
    assertThat(saved.get(0).getBookId()).isEqualTo(bookWithReviews);
    assertThat(saved.get(0).getRank()).isEqualTo(1L);
    assertThat(saved.get(1).getBookId()).isEqualTo(earlierSameReviewsBookId);
    assertThat(saved.get(1).getRank()).isEqualTo(2L);
    assertThat(saved.get(2).getBookId()).isEqualTo(bookWithoutReviews);
    assertThat(saved.get(2).getRank()).isEqualTo(3L);
    assertThat(saved.get(3).getBookId()).isEqualTo(laterSameReviewsBookId);
    assertThat(saved.get(3).getRank()).isEqualTo(4L);
  }

  // [인기 리뷰 집계]
  // 점수 공식: (좋아요 수 * 0.3) + (댓글 수 * 0.7)
  // 동점 처리: 점수 -> 댓글 수 -> 작성 시각(createdAt) 오름차순으로 우선순위 결정
  @Test
  @DisplayName("인기 리뷰 점수와 동점 시 먼저 작성된 리뷰 우선 정렬을 적용한다")
  void aggregatePopularReviews_calculatesScoreAndBreaksTieByCreatedAt() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 25);
    Period period = Period.DAILY;
    Instant start = baseDate.minusDays(1).atStartOfDay(KST).toInstant();
    Instant end = baseDate.atStartOfDay(KST).toInstant();

    UUID highScoreReviewId = UUID.randomUUID();
    UUID laterTieReviewId = UUID.randomUUID();
    UUID earlierTieReviewId = UUID.randomUUID();

    // highScoreReview: 좋아요 4(1.2) + 댓글 2(1.4) = 2.6점
    // earlierTieReview vs laterTieReview: 좋아요 2(0.6) + 댓글 1(0.7) = 1.3점 동점 -> earlier가 먼저
    when(aggregationRepository.findPopularReviewRaw(start, end))
        .thenReturn(List.of(
            reviewRaw(
                laterTieReviewId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "후순위유저",
                2L,
                1L,
                Instant.parse("2026-11-24T10:00:00Z")
            ),
            reviewRaw(
                highScoreReviewId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "최고점유저",
                4L,
                2L,
                Instant.parse("2026-11-24T12:00:00Z")
            ),
            reviewRaw(
                earlierTieReviewId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "선순위유저",
                2L,
                1L,
                Instant.parse("2026-11-24T05:00:00Z")
            )
        ));

    // when
    service.aggregatePopularReviews(period, baseDate, start, end);

    // then
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PopularReview>> captor = ArgumentCaptor.forClass(List.class);
    verify(popularReviewRepository).deleteAllByPeriodAndBaseDate(period, baseDate);
    verify(popularReviewRepository).saveAll(captor.capture());

    List<PopularReview> saved = captor.getValue();
    assertThat(saved).hasSize(3);
    assertThat(saved.get(0).getReviewId()).isEqualTo(highScoreReviewId);
    assertThat(saved.get(0).getRank()).isEqualTo(1L);
    assertThat(saved.get(0).getScore()).isCloseTo(2.6, within(1e-6));

    assertThat(saved.get(1).getReviewId()).isEqualTo(earlierTieReviewId);
    assertThat(saved.get(1).getRank()).isEqualTo(2L);
    assertThat(saved.get(1).getScore()).isCloseTo(1.3, within(1e-6));

    assertThat(saved.get(2).getReviewId()).isEqualTo(laterTieReviewId);
    assertThat(saved.get(2).getRank()).isEqualTo(3L);
  }

  // [파워 유저 집계]
  // 점수 공식: (작성 리뷰 인기 점수 합 * 0.5) + (참여 좋아요 수 * 0.2) + (참여 댓글 수 * 0.3)
  // 리뷰가 없는 댓글 활동 유저도 집계 대상에 포함되는지 함께 확인
  @Test
  @DisplayName("파워 유저 점수는 리뷰 인기 점수 합, 좋아요 수, 댓글 수 가중치로 계산한다")
  void aggregatePowerUsers_calculatesWeightedScore() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 25);
    Period period = Period.DAILY;
    Instant start = baseDate.minusDays(1).atStartOfDay(KST).toInstant();
    Instant end = baseDate.atStartOfDay(KST).toInstant();

    UUID activeUserId = UUID.randomUUID();
    UUID commentOnlyUserId = UUID.randomUUID();

    // activeUser: 리뷰 인기점수 (2*0.3 + 1*0.7) = 1.3 -> (1.3 * 0.5) + (2 * 0.2) + (2 * 0.3) = 1.65
    when(aggregationRepository.findUserReviewActivityRaw(start, end))
        .thenReturn(List.of(
            userReviewRaw(
                activeUserId,
                "리뷰작성유저",
                Instant.parse("2026-10-01T00:00:00Z"),
                UUID.randomUUID(),
                2L,
                1L
            )
        ));

    // commentOnlyUser: 댓글 3건 -> 3 * 0.3 = 0.9
    when(aggregationRepository.findUserCommentCountsRaw(start, end))
        .thenReturn(List.of(
            userCommentRaw(activeUserId, "리뷰작성유저", Instant.parse("2026-10-01T00:00:00Z"), 2L),
            userCommentRaw(commentOnlyUserId, "댓글활동유저", Instant.parse("2026-10-02T00:00:00Z"), 3L)
        ));

    // when
    service.aggregatePowerUsers(period, baseDate, start, end);

    // then
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PowerUser>> captor = ArgumentCaptor.forClass(List.class);
    verify(powerUserRepository).deleteAllByPeriodAndBaseDate(period, baseDate);
    verify(powerUserRepository).saveAll(captor.capture());

    List<PowerUser> saved = captor.getValue();
    assertThat(saved).hasSize(2);
    assertThat(saved.get(0).getUserId()).isEqualTo(activeUserId);
    assertThat(saved.get(0).getRank()).isEqualTo(1L);
    assertThat(saved.get(0).getScore()).isCloseTo(1.65, within(1e-6));

    assertThat(saved.get(1).getUserId()).isEqualTo(commentOnlyUserId);
    assertThat(saved.get(1).getRank()).isEqualTo(2L);
    assertThat(saved.get(1).getScore()).isCloseTo(0.9, within(1e-6));
  }

  // [널 지표 및 빈 데이터 처리]
  // 집계 대상 데이터가 비어 있거나 null 카운트/평점이 들어와도 0으로 안전하게 처리
  @Test
  @DisplayName("집계 지표가 null이거나 데이터가 비어 있어도 예외 없이 처리한다")
  void aggregate_handlesNullMetricsAndEmptyListsSafely() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 25);
    Instant start = baseDate.minusDays(1).atStartOfDay(KST).toInstant();
    Instant end = baseDate.atStartOfDay(KST).toInstant();

    UUID nullMetricBookId = UUID.randomUUID();
    when(aggregationRepository.findPopularBookRaw(start, end))
        .thenReturn(List.of(
            bookRaw(nullMetricBookId, "널지표책", 0L, 0.0, null)
        ));
    when(aggregationRepository.findPopularReviewRaw(start, end)).thenReturn(List.of());
    when(aggregationRepository.findUserReviewActivityRaw(start, end)).thenReturn(List.of());
    when(aggregationRepository.findUserCommentCountsRaw(start, end)).thenReturn(List.of());

    // when
    service.aggregatePopularBooks(Period.DAILY, baseDate, start, end);
    service.aggregatePopularReviews(Period.DAILY, baseDate, start, end);
    service.aggregatePowerUsers(Period.DAILY, baseDate, start, end);

    // then
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PopularBook>> bookCaptor = ArgumentCaptor.forClass(List.class);
    verify(popularBookRepository).saveAll(bookCaptor.capture());
    assertThat(bookCaptor.getValue()).hasSize(1);
    assertThat(bookCaptor.getValue().get(0).getScore()).isEqualTo(0.0);
    assertThat(bookCaptor.getValue().get(0).getRank()).isEqualTo(1L);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PopularReview>> reviewCaptor = ArgumentCaptor.forClass(List.class);
    verify(popularReviewRepository).saveAll(reviewCaptor.capture());
    assertThat(reviewCaptor.getValue()).isEmpty();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PowerUser>> userCaptor = ArgumentCaptor.forClass(List.class);
    verify(powerUserRepository).saveAll(userCaptor.capture());
    assertThat(userCaptor.getValue()).isEmpty();
  }

  // [기간별 구간 계산 검증]
  // WEEKLY(7일), MONTHLY(30일) 기준 KST 자정 경계가 리포지토리에 정확히 전달되는지 확인
  @Test
  @DisplayName("WEEKLY와 MONTHLY는 KST 자정 기준 7일·30일 구간으로 집계한다")
  void aggregateAll_weeklyAndMonthly_passesExpectedKstDateRanges() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 25);
    Instant expectedEnd = baseDate.atStartOfDay(KST).toInstant();
    Instant expectedWeeklyStart = baseDate.minusDays(7).atStartOfDay(KST).toInstant();
    Instant expectedMonthlyStart = baseDate.minusDays(30).atStartOfDay(KST).toInstant();

    when(aggregationRepository.findPopularBookRaw(any(), any())).thenReturn(List.of());
    when(aggregationRepository.findPopularReviewRaw(any(), any())).thenReturn(List.of());
    when(aggregationRepository.findUserReviewActivityRaw(any(), any())).thenReturn(List.of());
    when(aggregationRepository.findUserCommentCountsRaw(any(), any())).thenReturn(List.of());

    // when
    service.aggregateAll(Period.WEEKLY, baseDate);
    service.aggregateAll(Period.MONTHLY, baseDate);

    // then
    verify(aggregationRepository, times(1))
        .findPopularBookRaw(eq(expectedWeeklyStart), eq(expectedEnd));
    verify(aggregationRepository, times(1))
        .findPopularBookRaw(eq(expectedMonthlyStart), eq(expectedEnd));
  }
}