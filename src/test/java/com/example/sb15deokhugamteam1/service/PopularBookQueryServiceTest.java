package com.example.sb15deokhugamteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponsePopularBookDto;
import com.example.sb15deokhugamteam1.dto.response.PopularBookDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularBook;
import com.example.sb15deokhugamteam1.mapper.dto.PopularBookMapper;
import com.example.sb15deokhugamteam1.repository.PopularBookRepository;
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
class PopularBookQueryServiceTest {

  @Mock
  private PopularBookRepository popularBookRepository;

  @Mock
  private PopularBookMapper popularBookMapper;

  @InjectMocks
  private PopularBookQueryService popularBookQueryService;

  @Test
  @DisplayName("다음 페이지가 존재하면 hasNext=true와 nextCursor, nextAfter를 반환한다")
  void getPopularBooks_whenHasNext_returnsNextCursorAndAfter() {
    // given
    Period period = Period.DAILY;
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    int size = 2;

    PopularBook first = createPopularBook(UUID.randomUUID(), "도서1", period, baseDate, 1L, 95.0);
    PopularBook second = createPopularBook(UUID.randomUUID(), "도서2", period, baseDate, 2L, 85.0);
    PopularBook third = createPopularBook(UUID.randomUUID(), "도서3", period, baseDate, 3L, 75.0);

    Instant secondCreatedAt = Instant.parse("2026-11-24T01:00:00Z");
    ReflectionTestUtils.setField(first, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(first, "createdAt", Instant.parse("2026-11-24T00:00:00Z"));
    ReflectionTestUtils.setField(second, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(second, "createdAt", secondCreatedAt);

    PopularBookDto firstDto = new PopularBookDto(
        first.getId(), first.getBookId(), first.getTitle(), first.getAuthor(),
        first.getThumbnailUrl(), first.getPeriod(), first.getRank(), first.getScore(),
        first.getReviewCount(), first.getRating(), first.getCreatedAt()
    );
    PopularBookDto secondDto = new PopularBookDto(
        second.getId(), second.getBookId(), second.getTitle(), second.getAuthor(),
        second.getThumbnailUrl(), second.getPeriod(), second.getRank(), second.getScore(),
        second.getReviewCount(), second.getRating(), second.getCreatedAt()
    );

    given(popularBookRepository.findRankingWithCursor(period, baseDate, null, PageRequest.of(0, size + 1)))
        .willReturn(List.of(first, second, third));
    given(popularBookMapper.toDto(first)).willReturn(firstDto);
    given(popularBookMapper.toDto(second)).willReturn(secondDto);

    // when
    CursorPageResponsePopularBookDto response = popularBookQueryService.getPopularBooks(
        period, baseDate, null, size
    );

    // then
    assertThat(response.content()).hasSize(2);
    assertThat(response.hasNext()).isTrue();
    assertThat(response.nextCursor()).isEqualTo("2");
    assertThat(response.nextAfter()).isEqualTo(secondCreatedAt);
    assertThat(response.size()).isEqualTo(size);
    then(popularBookMapper).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("마지막 페이지이면 hasNext=false이고 데이터가 없으면 커서값이 null이다")
  void getPopularBooks_whenEmpty_returnsEmptyPage() {
    // given
    Period period = Period.WEEKLY;
    LocalDate baseDate = LocalDate.of(2026, 11, 24);
    int size = 10;

    given(popularBookRepository.findRankingWithCursor(period, baseDate, null, PageRequest.of(0, size + 1)))
        .willReturn(List.of());

    // when
    CursorPageResponsePopularBookDto response = popularBookQueryService.getPopularBooks(
        period, baseDate, null, size
    );

    // then
    assertThat(response.content()).isEmpty();
    assertThat(response.hasNext()).isFalse();
    assertThat(response.nextCursor()).isNull();
    assertThat(response.nextAfter()).isNull();
    assertThat(response.size()).isEqualTo(size);
  }

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
        .reviewCount(10L)
        .rating(4.5)
        .build();
  }
}