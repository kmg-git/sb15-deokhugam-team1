package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponsePopularBookDto;
import com.example.sb15deokhugamteam1.dto.response.PopularBookDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularBook;
import com.example.sb15deokhugamteam1.mapper.dto.PopularBookMapper;
import com.example.sb15deokhugamteam1.repository.PopularBookRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularBookQueryService {

  private final PopularBookRepository popularBookRepository;
  private final PopularBookMapper popularBookMapper;

  public CursorPageResponsePopularBookDto getPopularBooks(
      Period period,
      LocalDate baseDate,
      Long cursorRank,
      String direction,
      int size
  ) {
    LocalDate targetBaseDate = (baseDate != null)
        ? baseDate
        : popularBookRepository.findTopByPeriodOrderByBaseDateDesc(period)
            .map(PopularBook::getBaseDate)
            .orElse(null);

    if (targetBaseDate == null) {
      return new CursorPageResponsePopularBookDto(
          List.of(),
          null,
          null,
          size,
          0L,
          false
      );
    }

    boolean descending = "DESC".equalsIgnoreCase(direction);
    PageRequest pageRequest = PageRequest.of(0, size + 1);

    List<PopularBook> fetched = descending
        ? popularBookRepository.findRankingWithCursorDesc(period, targetBaseDate, cursorRank, pageRequest)
        : popularBookRepository.findRankingWithCursorAsc(period, targetBaseDate, cursorRank, pageRequest);

    boolean hasNext = fetched.size() > size;
    List<PopularBook> pageEntities = hasNext ? fetched.subList(0, size) : fetched;
    List<PopularBookDto> content = pageEntities.stream()
        .map(popularBookMapper::toDto)
        .toList();

    String nextCursor = null;
    Instant nextAfter = null;
    if (!pageEntities.isEmpty()) {
      PopularBook last = pageEntities.get(pageEntities.size() - 1);
      nextCursor = String.valueOf(last.getRank());
      nextAfter = last.getCreatedAt();
    }

    return new CursorPageResponsePopularBookDto(
        content,
        nextCursor,
        nextAfter,
        size,
        (long) content.size(),
        hasNext
    );
  }
}