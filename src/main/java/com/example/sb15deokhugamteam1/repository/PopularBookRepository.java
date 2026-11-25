package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularBook;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID> {

  Optional<PopularBook> findTopByPeriodOrderByBaseDateDesc(Period period);

  Optional<PopularBook> findByPeriodAndBaseDateAndBookId(
      Period period,
      LocalDate baseDate,
      UUID bookId
  );

  boolean existsByPeriodAndBaseDateAndBookId(
      Period period,
      LocalDate baseDate,
      UUID bookId
  );

  List<PopularBook> findAllByPeriodAndBaseDateOrderByRankAsc(
      Period period,
      LocalDate baseDate
  );

  @Query("""
      SELECT pb
      FROM PopularBook pb
      WHERE pb.period = :period
        AND pb.baseDate = :baseDate
        AND (:cursorRank IS NULL OR pb.rank > :cursorRank)
      ORDER BY pb.rank ASC, pb.id ASC
      """)
  List<PopularBook> findRankingWithCursor(
      @Param("period") Period period,
      @Param("baseDate") LocalDate baseDate,
      @Param("cursorRank") Long cursorRank,
      Pageable pageable
  );

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
      DELETE FROM PopularBook pb
      WHERE pb.period = :period
        AND pb.baseDate = :baseDate
      """)
  void deleteAllByPeriodAndBaseDate(
      @Param("period") Period period,
      @Param("baseDate") LocalDate baseDate
  );
}