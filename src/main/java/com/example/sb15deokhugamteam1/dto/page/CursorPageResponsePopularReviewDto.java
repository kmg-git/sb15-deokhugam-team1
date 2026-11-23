package com.example.sb15deokhugamteam1.dto.page;

import com.example.sb15deokhugamteam1.dto.response.PopularReviewDto;
import java.time.Instant;
import java.util.List;

public record CursorPageResponsePopularReviewDto(
    List<PopularReviewDto> content,
    String nextCursor,
    Instant nextAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {
}
