package com.example.sb15deokhugamteam1.mapper.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sb15deokhugamteam1.dto.response.PowerUserDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PowerUser;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PowerUserMapperTest {

  private final PowerUserMapper mapper = new PowerUserMapper();

  @Test
  @DisplayName("toEntity 호출 시 전달한 값으로 PowerUser 엔티티를 생성한다")
  void toEntity_mapsFieldsCorrectly() {
    // given 파워 유저·랭킹 입력값 준비
    UUID userId = UUID.randomUUID();
    LocalDate baseDate = LocalDate.of(2026, 11, 24);

    // when toEntity 호출
    PowerUser entity = mapper.toEntity(
        userId,
        "파워유저",
        Period.ALL_TIME,
        baseDate,
        1L,
        120.0,
        80.0,
        50L,
        30L
    );

    // then 엔티티 주요 필드 매핑 검증
    assertThat(entity.getUserId()).isEqualTo(userId);
    assertThat(entity.getNickname()).isEqualTo("파워유저");
    assertThat(entity.getPeriod()).isEqualTo(Period.ALL_TIME);
    assertThat(entity.getBaseDate()).isEqualTo(baseDate);
    assertThat(entity.getRank()).isEqualTo(1L);
    assertThat(entity.getScore()).isEqualTo(120.0);
    assertThat(entity.getReviewScoreSum()).isEqualTo(80.0);
    assertThat(entity.getLikeCount()).isEqualTo(50L);
    assertThat(entity.getCommentCount()).isEqualTo(30L);
  }

  @Test
  @DisplayName("toDto 호출 시 PowerUser 엔티티를 PowerUserDto로 변환한다")
  void toDto_mapsEntityToDto() {
    // given createdAt이 세팅된 PowerUser 엔티티 준비
    UUID userId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-11-24T02:00:00Z");
    LocalDate baseDate = LocalDate.of(2026, 11, 24);

    PowerUser entity = mapper.toEntity(
        userId,
        "파워유저",
        Period.DAILY,
        baseDate,
        2L,
        90.0,
        60.0,
        30L,
        20L
    );
    ReflectionTestUtils.setField(entity, "createdAt", createdAt);

    // when toDto 호출
    PowerUserDto dto = mapper.toDto(entity);

    // then DTO 필드 매핑 검증
    assertThat(dto).isNotNull();
    assertThat(dto.userId()).isEqualTo(userId);
    assertThat(dto.nickname()).isEqualTo("파워유저");
    assertThat(dto.period()).isEqualTo(Period.DAILY);
    assertThat(dto.createdAt()).isEqualTo(createdAt);
    assertThat(dto.rank()).isEqualTo(2L);
    assertThat(dto.score()).isEqualTo(90.0);
    assertThat(dto.reviewScoreSum()).isEqualTo(60.0);
    assertThat(dto.likeCount()).isEqualTo(30L);
    assertThat(dto.commentCount()).isEqualTo(20L);
  }

  @Test
  @DisplayName("toDto에 null이 전달되면 null을 반환한다")
  void toDto_whenNull_returnsNull() {
    // given  when null 입력으로 toDto 호출
    PowerUserDto dto = mapper.toDto(null);

    // then null 반환 검증
    assertThat(dto).isNull();
  }
}