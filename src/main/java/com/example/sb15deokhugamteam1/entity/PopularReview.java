package com.example.sb15deokhugamteam1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "popular_reviews",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_popular_reviews_period_date_review",
            columnNames = {"period", "base_date", "review_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularReview {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "book_title", nullable = false)
  private String bookTitle;

  @Column(name = "book_thumbnail_url")
  private String bookThumbnailUrl;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "user_nickname", nullable = false)
  private String userNickname;

  @Column(name = "review_content", columnDefinition = "text", nullable = false)
  private String reviewContent;

  @Column(name = "review_rating", nullable = false)
  private Double reviewRating;

  @Enumerated(EnumType.STRING)
  @Column(name = "period", nullable = false)
  private Period period;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(name = "`rank`", nullable = false)
  private Long rank;

  @Column(name = "score", nullable = false)
  private Double score;

  @Column(name = "like_count", nullable = false)
  private Long likeCount;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Builder
  public PopularReview(
      UUID id,
      UUID reviewId,
      UUID bookId,
      String bookTitle,
      String bookThumbnailUrl,
      UUID userId,
      String userNickname,
      String reviewContent,
      Double reviewRating,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score,
      Long likeCount,
      Long commentCount,
      OffsetDateTime createdAt
  ) {
    this.id = id != null ? id : UUID.randomUUID();
    this.reviewId = reviewId;
    this.bookId = bookId;
    this.bookTitle = bookTitle;
    this.bookThumbnailUrl = bookThumbnailUrl;
    this.userId = userId;
    this.userNickname = userNickname;
    this.reviewContent = reviewContent;
    this.reviewRating = reviewRating;
    this.period = period;
    this.baseDate = baseDate;
    this.rank = rank;
    this.score = score;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
    this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
  }
}