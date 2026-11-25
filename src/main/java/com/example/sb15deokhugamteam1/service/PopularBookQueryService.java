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
      int size
  ) {
    // 1. 다음 페이지 여부 확인을 위해 size + 1건 조회
    List<PopularBook> fetched = popularBookRepository.findRankingWithCursor(
        period,
        baseDate,
        cursorRank,
        PageRequest.of(0, size + 1)
    );

    // 2. hasNext 판별 및 페이지 크기만큼 목록 자르기
    boolean hasNext = fetched.size() > size;
    List<PopularBook> pageEntities = hasNext ? fetched.subList(0, size) : fetched;
    List<PopularBookDto> content = pageEntities.stream()
        .map(popularBookMapper::toDto)
        .toList();

    // 3. 마지막 항목 기준 다음 커서 정보 계산
    String nextCursor = null;
    Instant nextAfter = null;
    if (!pageEntities.isEmpty()) {
      PopularBook last = pageEntities.get(pageEntities.size() - 1);
      nextCursor = String.valueOf(last.getRank());
      nextAfter = last.getCreatedAt();
    }

    // 4. 커서 페이지 응답 DTO 조립 후 반환
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