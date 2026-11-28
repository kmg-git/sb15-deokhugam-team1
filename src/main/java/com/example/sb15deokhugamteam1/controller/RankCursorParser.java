package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.exception.dashboard.InvalidCursorException;

/**
 * 순위(rank) 기반 커서 문자열을 Long 으로 변환한다. 비어 있으면 첫 페이지(null).
 */
final class RankCursorParser {

  private RankCursorParser() {
  }

  static Long parse(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    try {
      long value = Long.parseLong(cursor.trim());
      if (value < 0) {
        throw new InvalidCursorException(cursor);
      }
      return value;
    } catch (NumberFormatException e) {
      throw new InvalidCursorException(cursor);
    }
  }
}