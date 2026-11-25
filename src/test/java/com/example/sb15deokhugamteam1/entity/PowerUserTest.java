package com.example.sb15deokhugamteam1.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PowerUserTest {

  @Test
  @DisplayName("빌더로 PowerUser 생성 시 전달한 값이 정상 반영된다")
  void create_powerUser_withBuilder() {
    // given: 파워 유저 생성용 입력값 준비
    UUID userId = UUID.randomUUID();
    LocalDate baseDate = LocalDate.of(2026, 11, 24);

    // when: 빌더로 PowerUser 생성
    PowerUser powerUser = PowerUser.builder()
        .userId(userId)
        .nickname("리뷰마스터")
        .period(Period.MONTHLY)
        .baseDate(baseDate)
        .rank(2L)
        .score(76.5)
        .reviewScoreSum(50.0)
        .likeCount(30L)
        .commentCount(18L)
        .build();

    // then: 필드 값 검증
    assertThat(powerUser.getUserId()).isEqualTo(userId);
    assertThat(powerUser.getNickname()).isEqualTo("리뷰마스터");
    assertThat(powerUser.getPeriod()).isEqualTo(Period.MONTHLY);
    assertThat(powerUser.getBaseDate()).isEqualTo(baseDate);
    assertThat(powerUser.getRank()).isEqualTo(2L);
    assertThat(powerUser.getScore()).isEqualTo(76.5);
    assertThat(powerUser.getReviewScoreSum()).isEqualTo(50.0);
    assertThat(powerUser.getLikeCount()).isEqualTo(30L);
    assertThat(powerUser.getCommentCount()).isEqualTo(18L);
  }
}