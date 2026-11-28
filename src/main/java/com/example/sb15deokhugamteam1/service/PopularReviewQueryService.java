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
      String direction,
      int size
  ) {
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

    boolean descending = "DESC".equalsIgnoreCase(direction);
    PageRequest pageRequest = PageRequest.of(0, size + 1);

    List<PopularReview> fetched = descending
        ? popularReviewRepository.findRankingWithCursorDesc(period, targetBaseDate, cursorRank, pageRequest)
        : popularReviewRepository.findRankingWithCursorAsc(period, targetBaseDate, cursorRank, pageRequest);

    boolean hasNext = fetched.size() > size;
    List<PopularReview> pageEntities = hasNext ? fetched.subList(0, size) : fetched;
    List<PopularReviewDto> content = pageEntities.stream()
        .map(popularReviewMapper::toDto)
        .toList();

    String nextCursor = null;
    Instant nextAfter = null;
    if (!pageEntities.isEmpty()) {
      PopularReview last = pageEntities.get(pageEntities.size() - 1);
      nextCursor = String.valueOf(last.getRank());
      nextAfter = last.getCreatedAt();
    }

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