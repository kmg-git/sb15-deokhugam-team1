package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponsePopularBookDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.service.PopularBookQueryService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books/popular")
@RequiredArgsConstructor
public class PopularBookController {

  private final PopularBookQueryService popularBookQueryService;

  @GetMapping
  public ResponseEntity<CursorPageResponsePopularBookDto> getPopularBooks(
      @RequestParam(defaultValue = "DAILY") Period period,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    Long cursorRank = (cursor == null || cursor.isBlank()) ? null : Long.parseLong(cursor);
    CursorPageResponsePopularBookDto response = popularBookQueryService.getPopularBooks(
        period,
        null,
        cursorRank,
        limit
    );
    return ResponseEntity.ok(response);
  }
}