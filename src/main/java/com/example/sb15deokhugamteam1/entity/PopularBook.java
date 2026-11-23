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
    name = "popular_books",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_popular_books_period_date_book",
            columnNames = {"period", "base_date", "book_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularBook {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "author", nullable = false)
  private String author;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "period", nullable = false)
  private Period period;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(name = "`rank`", nullable = false)
  private Long rank;

  @Column(name = "score", nullable = false)
  private Double score;

  @Column(name = "review_count", nullable = false)
  private Long reviewCount;

  @Column(name = "rating", nullable = false)
  private Double rating;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Builder
  public PopularBook(
      UUID id,
      UUID bookId,
      String title,
      String author,
      String thumbnailUrl,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score,
      Long reviewCount,
      Double rating,
      OffsetDateTime createdAt
  ) {
    this.id = id != null ? id : UUID.randomUUID();
    this.bookId = bookId;
    this.title = title;
    this.author = author;
    this.thumbnailUrl = thumbnailUrl;
    this.period = period;
    this.baseDate = baseDate;
    this.rank = rank;
    this.score = score;
    this.reviewCount = reviewCount;
    this.rating = rating;
    this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
  }
}