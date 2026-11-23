package com.example.sb15deokhugamteam1.mapper.dto;


import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PowerUser;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PowerUserMapper {

  public PowerUser toEntity(
      UUID userId,
      String nickname,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score,
      Double reviewScoreSum,
      Long likeCount,
      Long commentCount
  ) {
    return PowerUser.builder()
        .userId(userId)
        .nickname(nickname)
        .period(period)
        .baseDate(baseDate)
        .rank(rank)
        .score(score)
        .reviewScoreSum(reviewScoreSum)
        .likeCount(likeCount)
        .commentCount(commentCount)
        .createdAt(OffsetDateTime.now())
        .build();
  }


  public PowerUserDto toDto(PowerUser entity) {
    if (entity == null) {
      return null;
    }
    return new PowerUserDto(
        entity.getId(),
        entity.getUserId(),
        entity.getNickname(),
        entity.getPeriod(),
        entity.getBaseDate(),
        entity.getRank(),
        entity.getScore(),
        entity.getReviewScoreSum(),
        entity.getLikeCount(),
        entity.getCommentCount(),
        entity.getCreatedAt()
    );
  }


  public record PowerUserDto(
      UUID id,
      UUID userId,
      String nickname,
      Period period,
      LocalDate baseDate,
      Long rank,
      Double score,
      Double reviewScoreSum,
      Long likeCount,
      Long commentCount,
      OffsetDateTime createdAt
  ) {}
}