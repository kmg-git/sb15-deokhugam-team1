package com.example.sb15deokhugamteam1.exception.dashboard;

import java.util.Map;

/**
 * 대시보드 목록 조회의 cursor 값이 0 이상의 정수가 아닐 때 던지는 예외. (HTTP 400)
 */
public class InvalidCursorException extends DashboardException {

  public InvalidCursorException(String cursor) {
    super(DashboardErrorCode.INVALID_CURSOR, Map.of("cursor", String.valueOf(cursor)));
  }
}