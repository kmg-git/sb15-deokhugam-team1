package com.example.sb15deokhugamteam1.config;

import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.service.DashboardAggregationService;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DashboardBatchConfig {

  private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

  private final DashboardAggregationService aggregationService;

  @Bean
  public Job dashboardRankingJob(
      JobRepository jobRepository,
      Step dashboardAggregationStep
  ) {
    return new JobBuilder("dashboardRankingJob", jobRepository)
        .start(dashboardAggregationStep)
        .build();
  }

  @Bean
  public Step dashboardAggregationStep(
      JobRepository jobRepository,
      Tasklet dashboardAggregationTasklet,
      PlatformTransactionManager transactionManager
  ) {
    return new StepBuilder("dashboardAggregationStep", jobRepository)
        .tasklet(dashboardAggregationTasklet, transactionManager)
        .build();
  }

  @Bean
  public Tasklet dashboardAggregationTasklet() {
    return (contribution, chunkContext) -> {
      Object rawBaseDate = chunkContext.getStepContext()
          .getJobParameters()
          .get("baseDate");
      LocalDate baseDate = (rawBaseDate != null && !rawBaseDate.toString().isBlank())
          ? LocalDate.parse(rawBaseDate.toString())
          : LocalDate.now(ZONE);

      for (Period period : Period.values()) {
        aggregationService.aggregateAll(period, baseDate);
      }
      return RepeatStatus.FINISHED;
    };
  }
}