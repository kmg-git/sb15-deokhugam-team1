package com.example.sb15deokhugamteam1.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularBook;
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
class PopularBookRepositoryTest {

  @Mock
  private PopularBookRepository popularBookRepository;

  @Test
  @DisplayName("저장 시 BaseEntity의 UUID id와 Instant createdAt이 포함된 엔티티를 반환한다")
  void save_returnsSavedEntityWithIdAndCreatedAt() {
    // given
    UUID bookId = UUID.randomUUID();
    PopularBook popularBook = createPopularBook(
        bookId,
        "클린 코드",
        Period.DAILY,
        LocalDate.of(2026, 11, 24),
        1L,
        98.5
    );
    PopularBook persisted = createPopularBook(
        bookId,
        "클린 코드",
        Period.DAILY,
        LocalDate.of(2026, 11, 24),
        1L,
        98.5
    );
    ReflectionTestUtils.setField(persisted, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(persisted, "createdAt", Instant.now());

    given(popularBookRepository.save(popularBook)).willReturn(persisted);

    // when
    PopularBook saved = popularBookRepository.save(popularBook);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getBookId()).isEqualTo(bookId);
    then(popularBookRepository).should().save(popularBook);
  }

  @Test
  @DisplayName("period, baseDate, bookId로 단건 조회 및 존재 여부를 확인할 수 있다")
  void findAndExistsByUniqueKey_returnsExpectedResult() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    UUID bookId = UUID.randomUUID();
    UUID missingBookId = UUID.randomUUID();
    PopularBook popularBook = createPopularBook(bookId, "오브젝트", Period.WEEKLY, baseDate, 1L, 90.0);

    given(popularBookRepository.findByPeriodAndBaseDateAndBookId(Period.WEEKLY, baseDate, bookId))
        .willReturn(Optional.of(popularBook));
    given(popularBookRepository.existsByPeriodAndBaseDateAndBookId(Period.WEEKLY, baseDate, bookId))
        .willReturn(true);
    given(popularBookRepository.existsByPeriodAndBaseDateAndBookId(Period.WEEKLY, baseDate, missingBookId))
        .willReturn(false);

    // when
    Optional<PopularBook> found = popularBookRepository.findByPeriodAndBaseDateAndBookId(
        Period.WEEKLY, baseDate, bookId
    );
    boolean exists = popularBookRepository.existsByPeriodAndBaseDateAndBookId(
        Period.WEEKLY, baseDate, bookId
    );
    boolean notExists = popularBookRepository.existsByPeriodAndBaseDateAndBookId(
        Period.WEEKLY, baseDate, missingBookId
    );

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getBookId()).isEqualTo(bookId);
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("커서 유무에 따라 rank 오름차순 페이지를 반환한다")
  void findRankingWithCursor_returnsOrderedPage() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    Pageable firstPageable = PageRequest.of(0, 2);
    Pageable secondPageable = PageRequest.of(0, 2);

    PopularBook book1 = createPopularBook(UUID.randomUUID(), "도서1", Period.DAILY, baseDate, 1L, 95.0);
    PopularBook book2 = createPopularBook(UUID.randomUUID(), "도서2", Period.DAILY, baseDate, 2L, 85.0);
    PopularBook book3 = createPopularBook(UUID.randomUUID(), "도서3", Period.DAILY, baseDate, 3L, 75.0);

    given(popularBookRepository.findRankingWithCursor(Period.DAILY, baseDate, null, firstPageable))
        .willReturn(List.of(book1, book2));
    given(popularBookRepository.findRankingWithCursor(Period.DAILY, baseDate, 2L, secondPageable))
        .willReturn(List.of(book3));

    // when
    List<PopularBook> firstPage = popularBookRepository.findRankingWithCursor(
        Period.DAILY, baseDate, null, firstPageable
    );
    List<PopularBook> nextPage = popularBookRepository.findRankingWithCursor(
        Period.DAILY, baseDate, 2L, secondPageable
    );

    // then
    assertThat(firstPage).extracting(PopularBook::getRank).containsExactly(1L, 2L);
    assertThat(nextPage).extracting(PopularBook::getRank).containsExactly(3L);
  }

  @Test
  @DisplayName("유니크 키 중복 저장 시 DataIntegrityViolationException이 발생한다")
  void save_duplicateUniqueKey_throwsDataIntegrityViolationException() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    UUID bookId = UUID.randomUUID();
    PopularBook duplicate = createPopularBook(bookId, "도서1 중복", Period.DAILY, baseDate, 2L, 80.0);

    willThrow(DataIntegrityViolationException.class)
        .given(popularBookRepository)
        .save(duplicate);

    // when & then
    assertThatThrownBy(() -> popularBookRepository.save(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("특정 period와 baseDate의 데이터를 순위순 조회하고 일괄 삭제할 수 있다")
  void findAllAndDeleteByPeriodAndBaseDate_worksAsExpected() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    PopularBook book1 = createPopularBook(UUID.randomUUID(), "도서1", Period.MONTHLY, baseDate, 1L, 90.0);
    PopularBook book2 = createPopularBook(UUID.randomUUID(), "도서2", Period.MONTHLY, baseDate, 2L, 80.0);

    given(popularBookRepository.findAllByPeriodAndBaseDateOrderByRankAsc(Period.MONTHLY, baseDate))
        .willReturn(List.of(book1, book2));

    // when
    List<PopularBook> ordered = popularBookRepository.findAllByPeriodAndBaseDateOrderByRankAsc(
        Period.MONTHLY, baseDate
    );
    popularBookRepository.deleteAllByPeriodAndBaseDate(Period.MONTHLY, baseDate);

    // then
    assertThat(ordered).extracting(PopularBook::getRank).containsExactly(1L, 2L);
    then(popularBookRepository).should().deleteAllByPeriodAndBaseDate(Period.MONTHLY, baseDate);
  }

  // 테스트용 PopularBook 생성 헬퍼 메서드
  private PopularBook createPopularBook(
      UUID bookId,
      String title,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score
  ) {
    return PopularBook.builder()
        .bookId(bookId)
        .title(title)
        .author("작성자")
        .thumbnailUrl("https://example.com/book.png")
        .period(period)
        .baseDate(baseDate)
        .rank(rank)
        .score(score)
        .reviewCount(12L)
        .rating(4.8)
        .build();
  }
}