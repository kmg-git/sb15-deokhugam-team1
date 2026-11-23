package com.example.sb15deokhugamteam1.dto.response;

import com.example.sb15deokhugamteam1.entity.Period;
import java.time.Instant;
import java.util.UUID;

public record PowerUserDto(
    UUID userId,
    String nickname,
    Period period,
    Instant createdAt,
    Long rank,
    Double score,
    Double reviewScoreSum,
    Long likeCount,
    Long commentCount
) {
}
