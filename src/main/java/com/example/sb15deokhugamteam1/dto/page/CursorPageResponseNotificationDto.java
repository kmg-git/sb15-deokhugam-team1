package com.example.sb15deokhugamteam1.dto.page;

import com.example.sb15deokhugamteam1.dto.response.NotificationDto;
import java.time.Instant;
import java.util.List;

public record CursorPageResponseNotificationDto(
    List<NotificationDto> content,
    String nextCursor,
    Instant nextAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {
}
