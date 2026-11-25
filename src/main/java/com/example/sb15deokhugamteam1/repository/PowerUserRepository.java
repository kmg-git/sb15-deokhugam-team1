package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PowerUser;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {

  Optional<PowerUser> findByPeriodAndBaseDateAndUserId(
      Period period,
      LocalDate baseDate,
      UUID userId
  );

  boolean existsByPeriodAndBaseDateAndUserId(
      Period period,
      LocalDate baseDate,
      UUID userId
  );

  List<PowerUser> findAllByPeriodAndBaseDateOrderByRankAsc(
      Period period,
      LocalDate baseDate
  );

  @Query("""
      SELECT pu
      FROM PowerUser pu
      WHERE pu.period = :period
        AND pu.baseDate = :baseDate
        AND (:cursorRank IS NULL OR pu.rank > :cursorRank)
      ORDER BY pu.rank ASC, pu.id ASC
      """)
  List<PowerUser> findRankingWithCursor(
      @Param("period") Period period,
      @Param("baseDate") LocalDate baseDate,
      @Param("cursorRank") Long cursorRank,
      Pageable pageable
  );

  void deleteAllByPeriodAndBaseDate(Period period, LocalDate baseDate);
}