package com.example.sb15deokhugamteam1.mapper.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sb15deokhugamteam1.dto.response.PopularBookDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularBook;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PopularBookMapperTest {

  private final PopularBookMapper mapper = new PopularBookMapper();

  @Test
  @DisplayName("toEntity 호출 시 전달한 값으로 PopularBook 엔티티를 생성한다")
  void toEntity_mapsFieldsCorrectly() {
    // given: 변환에 사용할 도서 랭킹 입력값 준비
    UUID bookId = UUID.randomUUID();
    LocalDate baseDate = LocalDate.of(2026, 11, 24);

    // when: toEntity 호출
    PopularBook entity = mapper.toEntity(
        bookId,
        "도서 제목",
        "저자명",
        "https://example.com/book.png",
        Period.DAILY,
        baseDate,
        1L,
        95.5,
        20L,
        4.8
    );

    // then 엔티티 주요 필드 매핑 검증
    assertThat(entity.getBookId()).isEqualTo(bookId);
    assertThat(entity.getTitle()).isEqualTo("도서 제목");
    assertThat(entity.getAuthor()).isEqualTo("저자명");
    assertThat(entity.getThumbnailUrl()).isEqualTo("https://example.com/book.png");
    assertThat(entity.getPeriod()).isEqualTo(Period.DAILY);
    assertThat(entity.getBaseDate()).isEqualTo(baseDate);
    assertThat(entity.getRank()).isEqualTo(1L);
    assertThat(entity.getScore()).isEqualTo(95.5);
    assertThat(entity.getReviewCount()).isEqualTo(20L);
    assertThat(entity.getRating()).isEqualTo(4.8);
  }

  @Test
  @DisplayName("toDto 호출 시 PopularBook 엔티티를 PopularBookDto로 변환한다")
  void toDto_mapsEntityToDto() {
    // given id와 createdAt이 세팅된 PopularBook 엔티티 준비
    UUID id = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-11-24T00:00:00Z");
    LocalDate baseDate = LocalDate.of(2026, 11, 24);

    PopularBook entity = mapper.toEntity(
        bookId,
        "도서 제목",
        "저자명",
        "https://example.com/book.png",
        Period.WEEKLY,
        baseDate,
        2L,
        80.0,
        10L,
        4.0
    );
    ReflectionTestUtils.setField(entity, "id", id);
    ReflectionTestUtils.setField(entity, "createdAt", createdAt);

    // when toDto 호출
    PopularBookDto dto = mapper.toDto(entity);

    // then DTO 필드 매핑 검증
    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(id);
    assertThat(dto.bookId()).isEqualTo(bookId);
    assertThat(dto.title()).isEqualTo("도서 제목");
    assertThat(dto.author()).isEqualTo("저자명");
    assertThat(dto.thumbnailUrl()).isEqualTo("https://example.com/book.png");
    assertThat(dto.period()).isEqualTo(Period.WEEKLY);
    assertThat(dto.rank()).isEqualTo(2L);
    assertThat(dto.score()).isEqualTo(80.0);
    assertThat(dto.reviewCount()).isEqualTo(10L);
    assertThat(dto.rating()).isEqualTo(4.0);
    assertThat(dto.createdAt()).isEqualTo(createdAt);
  }

  @Test
  @DisplayName("toDto에 null이 전달되면 null을 반환한다")
  void toDto_whenNull_returnsNull() {
    // given  when null 입력으로 toDto 호출
    PopularBookDto dto = mapper.toDto(null);

    // then null 반환 검증
    assertThat(dto).isNull();
  }
}