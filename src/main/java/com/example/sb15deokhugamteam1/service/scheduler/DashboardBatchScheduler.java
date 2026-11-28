package com.example.sb15deokhugamteam1.service.scheduler;

import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.service.DashboardAggregationService;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 대시보드 랭킹 스냅샷을 매일 자정(Asia/Seoul)에 집계하는 배치 스케줄러.
 baseDate는 "오늘"이고, 집계 구간은 [baseDate - N일 00:00, baseDate 00:00) 이므로
 자정에 실행하면 직전 N일(DAILY는 어제 하루)의 데이터가 집계됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardBatchScheduler {

  private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

  private final DashboardAggregationService aggregationService;

  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  public void runDailyAggregation() {
    runAggregation(LocalDate.now(ZONE));
  }

  /**
   특정 기준일로 전체 기간을 집계합니다. (수동 재실행, 누락분 보정에도 사용 가능)
   한 기간이 실패해도 나머지 기간은 계속 진행합니다.
   */
  public void runAggregation(LocalDate baseDate) {
    log.info("대시보드 집계 시작 baseDate={}", baseDate);

    for (Period period : Period.values()) {
      long startMillis = System.currentTimeMillis();
      try {
        aggregationService.aggregateAll(period, baseDate);
        log.info("대시보드 집계 완료 period={}, baseDate={}, elapsed={}ms",
            period, baseDate, System.currentTimeMillis() - startMillis);
      } catch (Exception e) {
        log.error("대시보드 집계 실패 period={}, baseDate={}", period, baseDate, e);
      }
    }

    log.info("대시보드 집계 종료 baseDate={}", baseDate);
  }
}