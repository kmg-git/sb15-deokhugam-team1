package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularBook;
import com.example.sb15deokhugamteam1.entity.PopularReview;
import com.example.sb15deokhugamteam1.entity.PowerUser;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.PopularBookRaw;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.PopularReviewRaw;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.ReviewLikeCountRaw;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.UserCommentCountRaw;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.UserLikeCountRaw;
import com.example.sb15deokhugamteam1.repository.DashboardAggregationRepository.UserReviewActivityRaw;
import com.example.sb15deokhugamteam1.repository.PopularBookRepository;
import com.example.sb15deokhugamteam1.repository.PopularReviewRepository;
import com.example.sb15deokhugamteam1.repository.PowerUserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 대시보드 랭킹 스냅샷 집계 서비스.
 * 파워 유저 점수 계산에 인기 리뷰 점수가 반영되므로
 * 인기 도서 -> 인기 리뷰 -> 파워 유저 순서로 실행합니다.
 */
@Service
@RequiredArgsConstructor
public class DashboardAggregationService {

  private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

  private final DashboardAggregationRepository aggregationRepository;
  private final PopularBookRepository popularBookRepository;
  private final PopularReviewRepository popularReviewRepository;
  private final PowerUserRepository powerUserRepository;

  /**
   * 특정 기간과 기준일에 대해 인기 도서·인기 리뷰·파워 유저 스냅샷을 순서대로 집계합니다.
   */
  @Transactional
  public void aggregateAll(Period period, LocalDate baseDate) {
    Instant[] range = resolvePeriodRange(period, baseDate);
    aggregatePopularBooks(period, baseDate, range[0], range[1]);
    aggregatePopularReviews(period, baseDate, range[0], range[1]);
    aggregatePowerUsers(period, baseDate, range[0], range[1]);
  }

  /**
   * 1. 인기 도서 집계: (리뷰 수 * 0.4) + (평균 평점 * 0.6)
   */
  @Transactional
  public void aggregatePopularBooks(
      Period period,
      LocalDate baseDate,
      Instant startInclusive,
      Instant endExclusive
  ) {
    popularBookRepository.deleteAllByPeriodAndBaseDate(period, baseDate);

    List<PopularBookRaw> rawList =
        aggregationRepository.findPopularBookRaw(startInclusive, endExclusive);

    record ScoredBook(PopularBookRaw raw, long reviewCount, double avgRating, double score) {}

    List<ScoredBook> ranked = rawList.stream()
        .map(raw -> {
          long reviewCount = raw.getReviewCount() != null ? raw.getReviewCount() : 0L;
          double avgRating = raw.getAverageRating() != null ? raw.getAverageRating() : 0.0;
          double score = (reviewCount * 0.4) + (avgRating * 0.6);
          return new ScoredBook(raw, reviewCount, avgRating, score);
        })
        .sorted(
            Comparator.comparingDouble(ScoredBook::score).reversed()
                .thenComparing(Comparator.comparingLong(ScoredBook::reviewCount).reversed())
                .thenComparing(s -> s.raw().getBookCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
        )
        .toList();

    AtomicLong rankCounter = new AtomicLong(1L);
    List<PopularBook> snapshots = ranked.stream()
        .map(item -> PopularBook.builder()
            .bookId(item.raw().getBookId())
            .title(item.raw().getTitle())
            .author(item.raw().getAuthor())
            .thumbnailUrl(item.raw().getThumbnailUrl())
            .period(period)
            .baseDate(baseDate)
            .rank(rankCounter.getAndIncrement())
            .score(item.score())
            .reviewCount(item.reviewCount())
            .rating(item.avgRating())
            .build())
        .toList();

    popularBookRepository.saveAll(snapshots);
  }

  /**
   * 2. 인기 리뷰 집계: (좋아요 수 * 0.3) + (댓글 수 * 0.7)
   */
  @Transactional
  public void aggregatePopularReviews(
      Period period,
      LocalDate baseDate,
      Instant startInclusive,
      Instant endExclusive
  ) {
    popularReviewRepository.deleteAllByPeriodAndBaseDate(period, baseDate);

    List<PopularReviewRaw> rawList =
        aggregationRepository.findPopularReviewRaw(startInclusive, endExclusive);
    Map<UUID, Long> reviewLikeCountMap = getReviewLikeCountMap(startInclusive, endExclusive);

    record ScoredReview(PopularReviewRaw raw, long likeCount, long commentCount, double score) {}

    List<ScoredReview> ranked = rawList.stream()
        .map(raw -> {
          long likeCount = reviewLikeCountMap.getOrDefault(raw.getReviewId(), 0L);
          long commentCount = raw.getPeriodCommentCount() != null ? raw.getPeriodCommentCount() : 0L;
          double score = (likeCount * 0.3) + (commentCount * 0.7);
          return new ScoredReview(raw, likeCount, commentCount, score);
        })
        .sorted(
            Comparator.comparingDouble(ScoredReview::score).reversed()
                .thenComparing(Comparator.comparingLong(ScoredReview::commentCount).reversed())
                .thenComparing(s -> s.raw().getReviewCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
        )
        .toList();

    AtomicLong rankCounter = new AtomicLong(1L);
    List<PopularReview> snapshots = ranked.stream()
        .map(item -> {
          PopularReviewRaw raw = item.raw();
          return PopularReview.builder()
              .reviewId(raw.getReviewId())
              .bookId(raw.getBookId())
              .bookTitle(raw.getBookTitle())
              .bookThumbnailUrl(raw.getBookThumbnailUrl())
              .userId(raw.getUserId())
              .userNickname(raw.getUserNickname())
              .reviewContent(raw.getReviewContent())
              .reviewRating(raw.getReviewRating() != null ? raw.getReviewRating() : 0.0)
              .period(period)
              .baseDate(baseDate)
              .rank(rankCounter.getAndIncrement())
              .score(item.score())
              .likeCount(item.likeCount())
              .commentCount(item.commentCount())
              .build();
        })
        .toList();

    popularReviewRepository.saveAll(snapshots);
  }

  /**
   * 3. 파워 유저 집계: (작성 리뷰 인기 점수 합 * 0.5) + (좋아요 수 * 0.2) + (댓글 수 * 0.3)
   */
  @Transactional
  public void aggregatePowerUsers(
      Period period,
      LocalDate baseDate,
      Instant startInclusive,
      Instant endExclusive
  ) {
    powerUserRepository.deleteAllByPeriodAndBaseDate(period, baseDate);

    List<UserReviewActivityRaw> reviewActivities =
        aggregationRepository.findUserReviewActivityRaw(startInclusive, endExclusive);
    Map<UUID, Long> reviewLikeCountMap = getReviewLikeCountMap(startInclusive, endExclusive);
    List<UserLikeCountRaw> participatedLikes =
        aggregationRepository.findUserParticipatedLikeCountsRaw(startInclusive, endExclusive);
    List<UserCommentCountRaw> commentCounts =
        aggregationRepository.findUserCommentCountsRaw(startInclusive, endExclusive);

    record UserInfo(String nickname, Instant createdAt) {}

    Map<UUID, UserInfo> userInfoMap = new HashMap<>();
    Map<UUID, Double> reviewScoreSumMap = new HashMap<>();
    Map<UUID, Long> likeCountMap = new HashMap<>();
    Map<UUID, Long> commentCountMap = new HashMap<>();

    for (UserReviewActivityRaw row : reviewActivities) {
      UUID userId = row.getUserId();
      userInfoMap.putIfAbsent(userId, new UserInfo(row.getNickname(), row.getUserCreatedAt()));

      long reviewLikes = reviewLikeCountMap.getOrDefault(row.getReviewId(), 0L);
      long reviewComments = row.getReviewPeriodCommentCount() != null ? row.getReviewPeriodCommentCount() : 0L;
      double reviewPopularScore = (reviewLikes * 0.3) + (reviewComments * 0.7);

      reviewScoreSumMap.merge(userId, reviewPopularScore, Double::sum);
    }

    for (UserLikeCountRaw row : participatedLikes) {
      UUID userId = row.getUserId();
      userInfoMap.putIfAbsent(userId, new UserInfo(row.getNickname(), row.getUserCreatedAt()));
      long likes = row.getLikeCount() != null ? row.getLikeCount() : 0L;
      likeCountMap.merge(userId, likes, Long::sum);
    }

    for (UserCommentCountRaw row : commentCounts) {
      UUID userId = row.getUserId();
      userInfoMap.putIfAbsent(userId, new UserInfo(row.getNickname(), row.getUserCreatedAt()));
      long comments = row.getCommentCount() != null ? row.getCommentCount() : 0L;
      commentCountMap.merge(userId, comments, Long::sum);
    }

    record ScoredUser(
        UUID userId,
        String nickname,
        Instant createdAt,
        double reviewScoreSum,
        long likeCount,
        long commentCount,
        double score
    ) {}

    List<ScoredUser> ranked = userInfoMap.entrySet().stream()
        .map(entry -> {
          UUID userId = entry.getKey();
          UserInfo info = entry.getValue();
          double reviewScoreSum = reviewScoreSumMap.getOrDefault(userId, 0.0);
          long likeCount = likeCountMap.getOrDefault(userId, 0L);
          long commentCount = commentCountMap.getOrDefault(userId, 0L);
          double score = (reviewScoreSum * 0.5) + (likeCount * 0.2) + (commentCount * 0.3);
          return new ScoredUser(
              userId,
              info.nickname(),
              info.createdAt(),
              reviewScoreSum,
              likeCount,
              commentCount,
              score
          );
        })
        .sorted(
            Comparator.comparingDouble(ScoredUser::score).reversed()
                .thenComparing(ScoredUser::createdAt, Comparator.nullsLast(Comparator.naturalOrder()))
        )
        .toList();

    AtomicLong rankCounter = new AtomicLong(1L);
    List<PowerUser> snapshots = ranked.stream()
        .map(u -> PowerUser.builder()
            .userId(u.userId())
            .nickname(u.nickname())
            .period(period)
            .baseDate(baseDate)
            .rank(rankCounter.getAndIncrement())
            .score(u.score())
            .reviewScoreSum(u.reviewScoreSum())
            .likeCount(u.likeCount())
            .commentCount(u.commentCount())
            .build())
        .toList();

    powerUserRepository.saveAll(snapshots);
  }

  private Map<UUID, Long> getReviewLikeCountMap(Instant startInclusive, Instant endExclusive) {
    return aggregationRepository.findPeriodReviewLikeCountsRaw(startInclusive, endExclusive)
        .stream()
        .collect(Collectors.toMap(
            ReviewLikeCountRaw::getReviewId,
            raw -> raw.getLikeCount() != null ? raw.getLikeCount() : 0L,
            Long::sum
        ));
  }

  /**
   * 기간(DAILY, WEEKLY, MONTHLY, ALL_TIME)에 따른 조회 시각 구간 [startInclusive, endExclusive)을 계산합니다.
   */
  private Instant[] resolvePeriodRange(Period period, LocalDate baseDate) {
    Instant endExclusive = baseDate.atStartOfDay(DEFAULT_ZONE).toInstant();
    return switch (period) {
      case DAILY -> new Instant[]{baseDate.minusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant(), endExclusive};
      case WEEKLY -> new Instant[]{baseDate.minusDays(7).atStartOfDay(DEFAULT_ZONE).toInstant(), endExclusive};
      case MONTHLY -> new Instant[]{baseDate.minusDays(30).atStartOfDay(DEFAULT_ZONE).toInstant(), endExclusive};
      case ALL_TIME -> new Instant[]{null, null};
    };
  }
}