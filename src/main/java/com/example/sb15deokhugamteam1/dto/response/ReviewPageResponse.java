package com.example.sb15deokhugamteam1.dto.response;

import java.time.Instant;
import java.util.List;

public record ReviewPageResponse(
        List<ReviewDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
