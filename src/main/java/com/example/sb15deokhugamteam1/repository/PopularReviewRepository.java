package com.example.sb15deokhugamteam1.repository;


import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularReview;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

  Optional<PopularReview> findByPeriodAndBaseDateAndReviewId(
      Period period,
      LocalDate baseDate,
      UUID reviewId
  );

  boolean existsByPeriodAndBaseDateAndReviewId(
      Period period,
      LocalDate baseDate,
      UUID reviewId
  );

  List<PopularReview> findAllByPeriodAndBaseDateOrderByRankAsc(
      Period period,
      LocalDate baseDate
  );

  @Query("""
      SELECT pr
      FROM PopularReview pr
      WHERE pr.period = :period
        AND pr.baseDate = :baseDate
        AND (:cursorRank IS NULL OR pr.rank > :cursorRank)
      ORDER BY pr.rank ASC, pr.id ASC
      """)
  List<PopularReview> findRankingWithCursor(
      @Param("period") Period period,
      @Param("baseDate") LocalDate baseDate,
      @Param("cursorRank") Long cursorRank,
      Pageable pageable
  );

  void deleteAllByPeriodAndBaseDate(Period period, LocalDate baseDate);

}