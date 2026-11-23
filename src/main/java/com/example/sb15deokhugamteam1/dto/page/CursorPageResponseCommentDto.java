package com.example.sb15deokhugamteam1.dto.page;

import com.example.sb15deokhugamteam1.dto.response.CommentDto;
import java.time.Instant;
import java.util.List;

public record CursorPageResponseCommentDto(
    List<CommentDto> content,
    String nextCursor,
    Instant nextAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {
}
