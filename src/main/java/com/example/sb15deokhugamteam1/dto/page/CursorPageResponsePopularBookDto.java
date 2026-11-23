package com.example.sb15deokhugamteam1.dto.page;

import com.example.sb15deokhugamteam1.dto.response.PopularBookDto;
import java.time.Instant;
import java.util.List;

public record CursorPageResponsePopularBookDto(
    List<PopularBookDto> content,
    String nextCursor,
    Instant nextAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {
}
