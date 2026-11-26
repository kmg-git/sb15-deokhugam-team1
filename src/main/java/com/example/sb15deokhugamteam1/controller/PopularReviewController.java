package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponsePopularReviewDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.service.PopularReviewQueryService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews/popular")
@RequiredArgsConstructor
public class PopularReviewController {

  private final PopularReviewQueryService popularReviewQueryService;

  @GetMapping
  public ResponseEntity<CursorPageResponsePopularReviewDto> getPopularReviews(
      @RequestParam(defaultValue = "DAILY") Period period,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    Long cursorRank = (cursor == null || cursor.isBlank()) ? null : Long.parseLong(cursor);
    CursorPageResponsePopularReviewDto response = popularReviewQueryService.getPopularReviews(
        period,
        null,
        cursorRank,
        direction,
        limit
    );
    return ResponseEntity.ok(response);
  }
}