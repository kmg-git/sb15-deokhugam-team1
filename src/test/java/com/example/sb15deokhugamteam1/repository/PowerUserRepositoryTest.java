package com.example.sb15deokhugamteam1.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PowerUser;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PowerUserRepositoryTest {

  @Mock
  private PowerUserRepository powerUserRepository;

  @Test
  @DisplayName("저장 시 BaseEntity의 UUID id와 Instant createdAt이 포함된 엔티티를 반환한다")
  void save_returnsSavedEntityWithIdAndCreatedAt() {
    // given
    UUID userId = UUID.randomUUID();
    PowerUser powerUser = createPowerUser(
        userId,
        "파워유저1",
        Period.DAILY,
        LocalDate.of(2026, 11, 24),
        1L,
        92.0
    );
    PowerUser persisted = createPowerUser(
        userId,
        "파워유저1",
        Period.DAILY,
        LocalDate.of(2026, 11, 24),
        1L,
        92.0
    );
    ReflectionTestUtils.setField(persisted, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(persisted, "createdAt", Instant.now());

    given(powerUserRepository.save(powerUser)).willReturn(persisted);

    // when
    PowerUser saved = powerUserRepository.save(powerUser);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUserId()).isEqualTo(userId);
    then(powerUserRepository).should().save(powerUser);
  }

  @Test
  @DisplayName("period, baseDate, userId로 단건 조회 및 존재 여부를 확인할 수 있다")
  void findAndExistsByUniqueKey_returnsExpectedResult() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    UUID userId = UUID.randomUUID();
    UUID missingUserId = UUID.randomUUID();
    PowerUser powerUser = createPowerUser(userId, "유저A", Period.DAILY, baseDate, 1L, 90.0);

    given(powerUserRepository.findByPeriodAndBaseDateAndUserId(Period.DAILY, baseDate, userId))
        .willReturn(Optional.of(powerUser));
    given(powerUserRepository.existsByPeriodAndBaseDateAndUserId(Period.DAILY, baseDate, userId))
        .willReturn(true);
    given(powerUserRepository.existsByPeriodAndBaseDateAndUserId(Period.DAILY, baseDate, missingUserId))
        .willReturn(false);

    // when
    Optional<PowerUser> found = powerUserRepository.findByPeriodAndBaseDateAndUserId(
        Period.DAILY, baseDate, userId
    );
    boolean exists = powerUserRepository.existsByPeriodAndBaseDateAndUserId(
        Period.DAILY, baseDate, userId
    );
    boolean notExists = powerUserRepository.existsByPeriodAndBaseDateAndUserId(
        Period.DAILY, baseDate, missingUserId
    );

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getUserId()).isEqualTo(userId);
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("커서 유무에 따라 rank 오름차순 페이지를 반환한다")
  void findRankingWithCursor_returnsExpectedPages() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    Pageable firstPageable = PageRequest.of(0, 1);
    Pageable secondPageable = PageRequest.of(0, 2);

    PowerUser user1 = createPowerUser(UUID.randomUUID(), "유저1", Period.WEEKLY, baseDate, 1L, 95.0);
    PowerUser user2 = createPowerUser(UUID.randomUUID(), "유저2", Period.WEEKLY, baseDate, 2L, 80.0);
    PowerUser user3 = createPowerUser(UUID.randomUUID(), "유저3", Period.WEEKLY, baseDate, 3L, 70.0);

    given(powerUserRepository.findRankingWithCursor(Period.WEEKLY, baseDate, null, firstPageable))
        .willReturn(List.of(user1));
    given(powerUserRepository.findRankingWithCursor(Period.WEEKLY, baseDate, 1L, secondPageable))
        .willReturn(List.of(user2, user3));

    // when
    List<PowerUser> firstPage = powerUserRepository.findRankingWithCursor(
        Period.WEEKLY, baseDate, null, firstPageable
    );
    List<PowerUser> secondPage = powerUserRepository.findRankingWithCursor(
        Period.WEEKLY, baseDate, 1L, secondPageable
    );

    // then
    assertThat(firstPage).extracting(PowerUser::getRank).containsExactly(1L);
    assertThat(secondPage).extracting(PowerUser::getRank).containsExactly(2L, 3L);
  }

  @Test
  @DisplayName("유니크 키 중복 저장 시 DataIntegrityViolationException이 발생한다")
  void save_duplicateUniqueKey_throwsDataIntegrityViolationException() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    PowerUser duplicate = createPowerUser(
        UUID.randomUUID(), "유저1 중복", Period.DAILY, baseDate, 2L, 80.0
    );

    willThrow(DataIntegrityViolationException.class)
        .given(powerUserRepository)
        .save(duplicate);

    // when & then
    assertThatThrownBy(() -> powerUserRepository.save(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("특정 period와 baseDate의 데이터를 순위순 조회하고 일괄 삭제할 수 있다")
  void findAllAndDeleteByPeriodAndBaseDate_worksAsExpected() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    PowerUser user1 = createPowerUser(UUID.randomUUID(), "유저1", Period.MONTHLY, baseDate, 1L, 90.0);
    PowerUser user2 = createPowerUser(UUID.randomUUID(), "유저2", Period.MONTHLY, baseDate, 2L, 80.0);

    given(powerUserRepository.findAllByPeriodAndBaseDateOrderByRankAsc(Period.MONTHLY, baseDate))
        .willReturn(List.of(user1, user2));

    // when
    List<PowerUser> ordered = powerUserRepository.findAllByPeriodAndBaseDateOrderByRankAsc(
        Period.MONTHLY, baseDate
    );
    powerUserRepository.deleteAllByPeriodAndBaseDate(Period.MONTHLY, baseDate);

    // then
    assertThat(ordered).extracting(PowerUser::getRank).containsExactly(1L, 2L);
    then(powerUserRepository).should().deleteAllByPeriodAndBaseDate(Period.MONTHLY, baseDate);
  }

  // 테스트용 PowerUser 생성 헬퍼 메서드
  private PowerUser createPowerUser(
      UUID userId,
      String nickname,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score
  ) {
    return PowerUser.builder()
        .userId(userId)
        .nickname(nickname)
        .period(period)
        .baseDate(baseDate)
        .rank(rank)
        .score(score)
        .reviewScoreSum(45.0)
        .likeCount(30L)
        .commentCount(10L)
        .build();
  }
}