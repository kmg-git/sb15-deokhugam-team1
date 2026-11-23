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
    name = "power_users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_power_users_period_date_user",
            columnNames = {"period", "base_date", "user_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PowerUser extends BaseEntity{

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "nickname", nullable = false)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(name = "period", nullable = false)
  private Period period;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(name = "`rank`", nullable = false)
  private Long rank;

  @Column(name = "score", nullable = false)
  private Double score;

  @Column(name = "review_score_sum", nullable = false)
  private Double reviewScoreSum;

  @Column(name = "like_count", nullable = false)
  private Long likeCount;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount;

  @Builder
  public PowerUser(
      UUID userId,
      String nickname,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score,
      Double reviewScoreSum,
      Long likeCount,
      Long commentCount
  ) {
    this.userId = userId;
    this.nickname = nickname;
    this.period = period;
    this.baseDate = baseDate;
    this.rank = rank;
    this.score = score;
    this.reviewScoreSum = reviewScoreSum;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
  }
}