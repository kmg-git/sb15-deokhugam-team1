package com.example.sb15deokhugamteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponsePowerUserDto;
import com.example.sb15deokhugamteam1.dto.response.PowerUserDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PowerUser;
import com.example.sb15deokhugamteam1.mapper.dto.PowerUserMapper;
import com.example.sb15deokhugamteam1.repository.PowerUserRepository;
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
class PowerUserQueryServiceTest {

  @Mock
  private PowerUserRepository powerUserRepository;

  @Mock
  private PowerUserMapper powerUserMapper;

  @InjectMocks
  private PowerUserQueryService powerUserQueryService;

  @Test
  @DisplayName("다음 페이지가 존재하면 hasNext=true와 nextCursor, nextAfter를 반환한다")
  void getPowerUsers_whenHasNext_returnsNextCursorAndAfter() {
    // given
    Period period = Period.DAILY;
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    int size = 1;

    PowerUser first = createPowerUser(period, baseDate, 1L, 85.0);
    PowerUser second = createPowerUser(period, baseDate, 2L, 70.0);
    Instant firstCreatedAt = Instant.parse("2026-11-24T01:00:00Z");
    ReflectionTestUtils.setField(first, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(first, "createdAt", firstCreatedAt);

    PowerUserDto firstDto = new PowerUserDto(
        first.getUserId(),
        first.getNickname(),
        first.getPeriod(),
        first.getCreatedAt(),
        first.getRank(),
        first.getScore(),
        first.getReviewScoreSum(),
        first.getLikeCount(),
        first.getCommentCount()
    );

    given(powerUserRepository.findRankingWithCursor(period, baseDate, null, PageRequest.of(0, size + 1)))
        .willReturn(List.of(first, second));
    given(powerUserMapper.toDto(first)).willReturn(firstDto);

    // when
    CursorPageResponsePowerUserDto response = powerUserQueryService.getPowerUsers(
        period, baseDate, null, size
    );

    // then
    assertThat(response.content()).hasSize(1);
    assertThat(response.hasNext()).isTrue();
    assertThat(response.nextCursor()).isEqualTo("1");
    assertThat(response.nextAfter()).isEqualTo(firstCreatedAt);
    assertThat(response.size()).isEqualTo(size);
    then(powerUserMapper).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("데이터가 없으면 빈 목록과 hasNext=false, null 커서를 반환한다")
  void getPowerUsers_whenEmpty_returnsEmptyPage() {
    // given
    Period period = Period.MONTHLY;
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    int size = 10;

    given(powerUserRepository.findRankingWithCursor(period, baseDate, null, PageRequest.of(0, size + 1)))
        .willReturn(List.of());

    // when
    CursorPageResponsePowerUserDto response = powerUserQueryService.getPowerUsers(
        period, baseDate, null, size
    );

    // then
    assertThat(response.content()).isEmpty();
    assertThat(response.hasNext()).isFalse();
    assertThat(response.nextCursor()).isNull();
    assertThat(response.nextAfter()).isNull();
    assertThat(response.size()).isEqualTo(size);
  }

  // 테스트용 PowerUser 생성 헬퍼 메서드
  private PowerUser createPowerUser(
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score
  ) {
    return PowerUser.builder()
        .userId(UUID.randomUUID())
        .nickname("파워유저")
        .period(period)
        .baseDate(baseDate)
        .rank(rank)
        .score(score)
        .reviewScoreSum(40.0)
        .likeCount(25L)
        .commentCount(15L)
        .build();
  }
}