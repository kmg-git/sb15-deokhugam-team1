package com.example.sb15deokhugamteam1.dto.response;

import com.example.sb15deokhugamteam1.entity.Period;
import java.time.Instant;
import java.util.UUID;

public record PopularBookDto(
    UUID id,
    UUID bookId,
    String title,
    String author,
    String thumbnailUrl,
    Period period,
    Long rank,
    Double score,
    Long reviewCount,
    Double rating,
    Instant createdAt
) {
}
