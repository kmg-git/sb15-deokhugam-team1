package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Review;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 대시보드 랭킹 산출을 위해 원본 도메인(Book, Review, User, Comment)에서
 * 기간별 집계 원천 데이터를 조회하는 리포지토리.
 */
public interface DashboardAggregationRepository extends JpaRepository<Review, UUID> {

  interface PopularBookRaw {
    UUID getBookId();
    String getTitle();
    String getAuthor();
    String getThumbnailUrl();
    Long getReviewCount();
    Double getAverageRating();
    Instant getBookCreatedAt();
  }

  interface PopularReviewRaw {
    UUID getReviewId();
    UUID getBookId();
    String getBookTitle();
    String getBookThumbnailUrl();
    UUID getUserId();
    String getUserNickname();
    String getReviewContent();
    Double getReviewRating();
    Long getPeriodCommentCount();
    Instant getReviewCreatedAt();
  }

  interface ReviewLikeCountRaw {
    UUID getReviewId();
    Long getLikeCount();
  }

  interface UserReviewActivityRaw {
    UUID getUserId();
    String getNickname();
    Instant getUserCreatedAt();
    UUID getReviewId();
    Long getReviewPeriodCommentCount();
  }

  interface UserLikeCountRaw {
    UUID getUserId();
    String getNickname();
    Instant getUserCreatedAt();
    Long getLikeCount();
  }

  interface UserCommentCountRaw {
    UUID getUserId();
    String getNickname();
    Instant getUserCreatedAt();
    Long getCommentCount();
  }

  /**
   * 1. 인기 도서 집계 원천: 활성 도서·활성 리뷰 기준 기간 내 리뷰 수와 평균 평점
   */
  @Query("""
      SELECT
        b.id AS bookId,
        b.title AS title,
        b.author AS author,
        b.thumbnailUrl AS thumbnailUrl,
        COUNT(r.id) AS reviewCount,
        AVG(CAST(r.rating AS double)) AS averageRating,
        b.createdAt AS bookCreatedAt
      FROM Review r
      JOIN r.book b
      WHERE r.deletedAt IS NULL
        AND b.deletedAt IS NULL
        AND (:startInclusive IS NULL OR r.createdAt >= :startInclusive)
        AND (:endExclusive IS NULL OR r.createdAt < :endExclusive)
      GROUP BY b.id, b.title, b.author, b.thumbnailUrl, b.createdAt
      """)
  List<PopularBookRaw> findPopularBookRaw(
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive
  );

  /**
   * 2. 인기 리뷰 집계 원천: 활성 리뷰·도서·작성자 기준 좋아요 수와 기간 내 댓글 수
   */
  @Query("""
      SELECT
        r.id AS reviewId,
        b.id AS bookId,
        b.title AS bookTitle,
        b.thumbnailUrl AS bookThumbnailUrl,
        u.id AS userId,
        u.nickname AS userNickname,
        r.content AS reviewContent,
        CAST(r.rating AS double) AS reviewRating,
        COUNT(c.id) AS periodCommentCount,
        r.createdAt AS reviewCreatedAt
      FROM Review r
      JOIN r.book b
      JOIN r.user u
      LEFT JOIN Comment c
        ON c.review = r
       AND (:startInclusive IS NULL OR c.createdAt >= :startInclusive)
       AND (:endExclusive IS NULL OR c.createdAt < :endExclusive)
      WHERE r.deletedAt IS NULL
        AND b.deletedAt IS NULL
        AND u.deletedAt IS NULL
        AND (:startInclusive IS NULL OR r.createdAt >= :startInclusive)
        AND (:endExclusive IS NULL OR r.createdAt < :endExclusive)
      GROUP BY
        r.id, b.id, b.title, b.thumbnailUrl,
        u.id, u.nickname, r.content, r.rating, r.createdAt
      """)
  List<PopularReviewRaw> findPopularReviewRaw(
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive
  );

  @Query("""
      SELECT
        rl.review.id AS reviewId,
        COUNT(rl.id) AS likeCount
      FROM ReviewLike rl
      JOIN rl.review r
      WHERE r.deletedAt IS NULL
        AND (:startInclusive IS NULL OR rl.createdAt >= :startInclusive)
        AND (:endExclusive IS NULL OR rl.createdAt < :endExclusive)
      GROUP BY rl.review.id
      """)
  List<ReviewLikeCountRaw> findPeriodReviewLikeCountsRaw(
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive
  );

  /**
   * 3-a. 파워 유저 집계 원천: 기간 내 작성한 활성 리뷰별 좋아요 수와 기간 내 댓글 수
   */
  @Query("""
      SELECT
        u.id AS userId,
        u.nickname AS nickname,
        u.createdAt AS userCreatedAt,
        r.id AS reviewId,
        COUNT(c.id) AS reviewPeriodCommentCount
      FROM Review r
      JOIN r.user u
      LEFT JOIN Comment c
        ON c.review = r
       AND (:startInclusive IS NULL OR c.createdAt >= :startInclusive)
       AND (:endExclusive IS NULL OR c.createdAt < :endExclusive)
      WHERE r.deletedAt IS NULL
        AND u.deletedAt IS NULL
        AND (:startInclusive IS NULL OR r.createdAt >= :startInclusive)
        AND (:endExclusive IS NULL OR r.createdAt < :endExclusive)
      GROUP BY u.id, u.nickname, u.createdAt, r.id
      """)
  List<UserReviewActivityRaw> findUserReviewActivityRaw(
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive
  );

  @Query("""
      SELECT
        u.id AS userId,
        u.nickname AS nickname,
        u.createdAt AS userCreatedAt,
        COUNT(rl.id) AS likeCount
      FROM ReviewLike rl
      JOIN rl.user u
      JOIN rl.review r
      WHERE u.deletedAt IS NULL
        AND r.deletedAt IS NULL
        AND (:startInclusive IS NULL OR rl.createdAt >= :startInclusive)
        AND (:endExclusive IS NULL OR rl.createdAt < :endExclusive)
      GROUP BY u.id, u.nickname, u.createdAt
      """)
  List<UserLikeCountRaw> findUserParticipatedLikeCountsRaw(
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive
  );

  /**
   * 3-b. 파워 유저 집계 원천: 기간 내 사용자가 작성한 댓글 수
   */
  @Query("""
      SELECT
        u.id AS userId,
        u.nickname AS nickname,
        u.createdAt AS userCreatedAt,
        COUNT(c.id) AS commentCount
      FROM Comment c
      JOIN c.user u
      JOIN c.review r
      WHERE u.deletedAt IS NULL
        AND r.deletedAt IS NULL
        AND (:startInclusive IS NULL OR c.createdAt >= :startInclusive)
        AND (:endExclusive IS NULL OR c.createdAt < :endExclusive)
      GROUP BY u.id, u.nickname, u.createdAt
      """)
  List<UserCommentCountRaw> findUserCommentCountsRaw(
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive
  );
}