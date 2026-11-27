package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.dto.response.NotificationDto;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationQueryRepository {

    List<NotificationDto> findPage(UUID userId, Sort.Direction direction, UUID cursorId, Instant after, int limit);
}
