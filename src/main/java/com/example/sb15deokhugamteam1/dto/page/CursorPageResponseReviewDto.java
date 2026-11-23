package com.example.sb15deokhugamteam1.dto.page;

import com.example.sb15deokhugamteam1.dto.response.ReviewDto;
import java.time.Instant;
import java.util.List;

public record CursorPageResponseReviewDto(
    List<ReviewDto> content,
    String nextCursor,
    Instant nextAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {
}
