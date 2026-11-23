package com.example.sb15deokhugamteam1.mapper.dto;


import com.example.sb15deokhugamteam1.dto.response.PopularBookDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PopularBook;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PopularBookMapper {

  public PopularBook toEntity(
      UUID bookId,
      String title,
      String author,
      String thumbnailUrl,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score,
      Long reviewCount,
      Double rating
  ) {
    return PopularBook.builder()
        .bookId(bookId)
        .title(title)
        .author(author)
        .thumbnailUrl(thumbnailUrl)
        .period(period)
        .baseDate(baseDate)
        .rank(rank)
        .score(score)
        .reviewCount(reviewCount)
        .rating(rating)
        .build();
  }


  public PopularBookDto toDto(PopularBook entity) {
    if (entity == null) {
      return null;
    }
    return new PopularBookDto(
        entity.getId(),
        entity.getBookId(),
        entity.getTitle(),
        entity.getAuthor(),
        entity.getThumbnailUrl(),
        entity.getPeriod(),
        entity.getRank(),
        entity.getScore(),
        entity.getReviewCount(),
        entity.getRating(),
        entity.getCreatedAt()
    );
  }
}
