package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponsePopularReviewDto;
import com.example.sb15deokhugamteam1.dto.response.PopularReviewDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularReview;
import com.example.sb15deokhugamteam1.mapper.dto.PopularReviewMapper;
import com.example.sb15deokhugamteam1.repository.PopularReviewRepository;
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
public class PopularReviewQueryService {

  private final PopularReviewRepository popularReviewRepository;
  private final PopularReviewMapper popularReviewMapper;

  public CursorPageResponsePopularReviewDto getPopularReviews(
      Period period,
      LocalDate baseDate,
      Long cursorRank,
      int size
  ) {
    // 0. baseDate 미지정 시 해당 기간의 가장 최근 스냅샷 기준일 조회
    LocalDate targetBaseDate = (baseDate != null)
        ? baseDate
        : popularReviewRepository.findTopByPeriodOrderByBaseDateDesc(period)
            .map(PopularReview::getBaseDate)
            .orElse(null);

    if (targetBaseDate == null) {
      return new CursorPageResponsePopularReviewDto(
          List.of(),
          null,
          null,
          size,
          0L,
          false
      );
    }

    // 1. 다음 페이지 여부 확인을 위해 size + 1건 조회
    List<PopularReview> fetched = popularReviewRepository.findRankingWithCursor(
        period,
        targetBaseDate,
        cursorRank,
        PageRequest.of(0, size + 1)
    );

    // 2. hasNext 판별 및 페이지 크기만큼 목록 자르기
    boolean hasNext = fetched.size() > size;
    List<PopularReview> pageEntities = hasNext ? fetched.subList(0, size) : fetched;
    List<PopularReviewDto> content = pageEntities.stream()
        .map(popularReviewMapper::toDto)
        .toList();

    // 3. 마지막 항목 기준 다음 커서 정보 계산
    String nextCursor = null;
    Instant nextAfter = null;
    if (!pageEntities.isEmpty()) {
      PopularReview last = pageEntities.get(pageEntities.size() - 1);
      nextCursor = String.valueOf(last.getRank());
      nextAfter = last.getCreatedAt();
    }

    // 4. 커서 페이지 응답 DTO 조립 후 반환
    return new CursorPageResponsePopularReviewDto(
        content,
        nextCursor,
        nextAfter,
        size,
        (long) content.size(),
        hasNext
    );
  }
}