package com.example.sb15deokhugamteam1.repository;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularReview;
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
class PopularReviewRepositoryTest {

  @Mock
  private PopularReviewRepository popularReviewRepository;

  @Test
  @DisplayName("저장 시 BaseEntity의 UUID id와 Instant createdAt이 포함된 엔티티를 반환한다")
  void save_returnsSavedEntityWithIdAndCreatedAt() {
    // given
    UUID reviewId = UUID.randomUUID();
    PopularReview popularReview = createPopularReview(
        reviewId,
        UUID.randomUUID(),
        UUID.randomUUID(),
        Period.DAILY,
        LocalDate.of(2026, 11, 24),
        1L,
        88.0
    );
    PopularReview persisted = createPopularReview(
        reviewId,
        popularReview.getBookId(),
        popularReview.getUserId(),
        Period.DAILY,
        LocalDate.of(2026, 11, 24),
        1L,
        88.0
    );
    ReflectionTestUtils.setField(persisted, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(persisted, "createdAt", Instant.now());

    given(popularReviewRepository.save(popularReview)).willReturn(persisted);

    // when
    PopularReview saved = popularReviewRepository.save(popularReview);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getReviewId()).isEqualTo(reviewId);
    then(popularReviewRepository).should().save(popularReview);
  }

  @Test
  @DisplayName("period, baseDate, reviewId로 단건 조회 및 존재 여부를 확인할 수 있다")
  void findAndExistsByUniqueKey_returnsExpectedResult() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    UUID reviewId = UUID.randomUUID();
    UUID missingReviewId = UUID.randomUUID();
    PopularReview popularReview = createPopularReview(
        reviewId, UUID.randomUUID(), UUID.randomUUID(), Period.DAILY, baseDate, 1L, 90.0
    );

    given(popularReviewRepository.findByPeriodAndBaseDateAndReviewId(Period.DAILY, baseDate, reviewId))
        .willReturn(Optional.of(popularReview));
    given(popularReviewRepository.existsByPeriodAndBaseDateAndReviewId(Period.DAILY, baseDate, reviewId))
        .willReturn(true);
    given(popularReviewRepository.existsByPeriodAndBaseDateAndReviewId(Period.DAILY, baseDate, missingReviewId))
        .willReturn(false);

    // when
    Optional<PopularReview> found = popularReviewRepository.findByPeriodAndBaseDateAndReviewId(
        Period.DAILY, baseDate, reviewId
    );
    boolean exists = popularReviewRepository.existsByPeriodAndBaseDateAndReviewId(
        Period.DAILY, baseDate, reviewId
    );
    boolean notExists = popularReviewRepository.existsByPeriodAndBaseDateAndReviewId(
        Period.DAILY, baseDate, missingReviewId
    );

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getReviewId()).isEqualTo(reviewId);
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

    PopularReview review1 = createPopularReview(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Period.WEEKLY, baseDate, 1L, 95.0
    );
    PopularReview review2 = createPopularReview(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Period.WEEKLY, baseDate, 2L, 80.0
    );
    PopularReview review3 = createPopularReview(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Period.WEEKLY, baseDate, 3L, 70.0
    );

    given(popularReviewRepository.findRankingWithCursorAsc(Period.WEEKLY, baseDate, null, firstPageable))
        .willReturn(List.of(review1));
    given(popularReviewRepository.findRankingWithCursorAsc(Period.WEEKLY, baseDate, 1L, secondPageable))
        .willReturn(List.of(review2, review3));

    // when
    List<PopularReview> firstPage = popularReviewRepository.findRankingWithCursorAsc(
        Period.WEEKLY, baseDate, null, firstPageable
    );
    List<PopularReview> secondPage = popularReviewRepository.findRankingWithCursorAsc(
        Period.WEEKLY, baseDate, 1L, secondPageable
    );

    // then
    assertThat(firstPage).extracting(PopularReview::getRank).containsExactly(1L);
    assertThat(secondPage).extracting(PopularReview::getRank).containsExactly(2L, 3L);
  }

  @Test
  @DisplayName("유니크 키 중복 저장 시 DataIntegrityViolationException이 발생한다")
  void save_duplicateUniqueKey_throwsDataIntegrityViolationException() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    PopularReview duplicate = createPopularReview(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Period.DAILY, baseDate, 2L, 80.0
    );

    willThrow(DataIntegrityViolationException.class)
        .given(popularReviewRepository)
        .save(duplicate);

    // when & then
    assertThatThrownBy(() -> popularReviewRepository.save(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("특정 period와 baseDate의 데이터를 순위순 조회하고 일괄 삭제할 수 있다")
  void findAllAndDeleteByPeriodAndBaseDate_worksAsExpected() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    PopularReview review1 = createPopularReview(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Period.ALL_TIME, baseDate, 1L, 90.0
    );
    PopularReview review2 = createPopularReview(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Period.ALL_TIME, baseDate, 2L, 80.0
    );

    given(popularReviewRepository.findAllByPeriodAndBaseDateOrderByRankAsc(Period.ALL_TIME, baseDate))
        .willReturn(List.of(review1, review2));

    // when
    List<PopularReview> ordered = popularReviewRepository.findAllByPeriodAndBaseDateOrderByRankAsc(
        Period.ALL_TIME, baseDate
    );
    popularReviewRepository.deleteAllByPeriodAndBaseDate(Period.ALL_TIME, baseDate);

    // then
    assertThat(ordered).extracting(PopularReview::getRank).containsExactly(1L, 2L);
    then(popularReviewRepository).should().deleteAllByPeriodAndBaseDate(Period.ALL_TIME, baseDate);
  }

  // 테스트용 PopularReview 생성 헬퍼 메서드
  private PopularReview createPopularReview(
      UUID reviewId,
      UUID bookId,
      UUID userId,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score
  ) {
    return PopularReview.builder()
        .reviewId(reviewId)
        .bookId(bookId)
        .bookTitle("인기 도서")
        .bookThumbnailUrl("https://example.com/thumb.png")
        .userId(userId)
        .userNickname("리뷰어")
        .reviewContent("정말 유익한 리뷰입니다.")
        .reviewRating(4.5)
        .period(period)
        .baseDate(baseDate)
        .rank(rank)
        .score(score)
        .likeCount(20L)
        .commentCount(5L)
        .build();
  }
}