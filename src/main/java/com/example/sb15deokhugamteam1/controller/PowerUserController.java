package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponsePowerUserDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.service.PowerUserQueryService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/power")
@RequiredArgsConstructor
public class PowerUserController {

  private final PowerUserQueryService powerUserQueryService;

  @GetMapping
  public ResponseEntity<CursorPageResponsePowerUserDto> getPowerUsers(
      @RequestParam(defaultValue = "DAILY") Period period,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    Long cursorRank = (cursor == null || cursor.isBlank()) ? null : Long.parseLong(cursor);
    CursorPageResponsePowerUserDto response = powerUserQueryService.getPowerUsers(
        period,
        null,
        cursorRank,
        limit
    );
    return ResponseEntity.ok(response);
  }
}