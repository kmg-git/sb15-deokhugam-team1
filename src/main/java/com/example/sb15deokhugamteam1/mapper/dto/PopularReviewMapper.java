package com.example.project.domain.dashboard.mapper.dto;


import com.example.sb15deokhugamteam1.dto.response.PopularReviewDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularReview;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PopularReviewMapper {

  public PopularReview toEntity(
      UUID reviewId,
      UUID bookId,
      String bookTitle,
      String bookThumbnailUrl,
      UUID userId,
      String userNickname,
      String reviewContent,
      Double reviewRating,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score,
      Long likeCount,
      Long commentCount
  ) {
    return PopularReview.builder()
        .reviewId(reviewId)
        .bookId(bookId)
        .bookTitle(bookTitle)
        .bookThumbnailUrl(bookThumbnailUrl)
        .userId(userId)
        .userNickname(userNickname)
        .reviewContent(reviewContent)
        .reviewRating(reviewRating)
        .period(period)
        .baseDate(baseDate)
        .rank(rank)
        .score(score)
        .likeCount(likeCount)
        .commentCount(commentCount)
        .createdAt(OffsetDateTime.now())
        .build();
  }


  public PopularReviewDto toDto(PopularReview entity) {
    if (entity == null) {
      return null;
    }
    return new PopularReviewDto(
        entity.getId(),
        entity.getReviewId(),
        entity.getBookId(),
        entity.getBookTitle(),
        entity.getBookThumbnailUrl(),
        entity.getUserId(),
        entity.getUserNickname(),
        entity.getReviewContent(),
        entity.getReviewRating(),
        entity.getPeriod(),
        entity.getCreatedAt(),
        entity.getRank(),
        entity.getScore(),
        entity.getLikeCount(),
        entity.getCommentCount()
    );
  }
}
