package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponsePowerUserDto;
import com.example.sb15deokhugamteam1.dto.response.PowerUserDto;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.PowerUser;
import com.example.sb15deokhugamteam1.mapper.dto.PowerUserMapper;
import com.example.sb15deokhugamteam1.repository.PowerUserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PowerUserQueryService {

  private final PowerUserRepository powerUserRepository;
  private final PowerUserMapper powerUserMapper;

  public CursorPageResponsePowerUserDto getPowerUsers(
      Period period,
      LocalDate baseDate,
      Long cursorRank,
      String direction,
      int size
  ) {
    LocalDate targetBaseDate = (baseDate != null)
        ? baseDate
        : powerUserRepository.findTopByPeriodOrderByBaseDateDesc(period)
            .map(PowerUser::getBaseDate)
            .orElse(null);

    if (targetBaseDate == null) {
      return new CursorPageResponsePowerUserDto(
          List.of(),
          null,
          null,
          size,
          0L,
          false
      );
    }

    boolean descending = "DESC".equalsIgnoreCase(direction);
    PageRequest pageRequest = PageRequest.of(0, size + 1);

    List<PowerUser> fetched = descending
        ? powerUserRepository.findRankingWithCursorDesc(period, targetBaseDate, cursorRank, pageRequest)
        : powerUserRepository.findRankingWithCursorAsc(period, targetBaseDate, cursorRank, pageRequest);

    boolean hasNext = fetched.size() > size;
    List<PowerUser> pageEntities = hasNext ? fetched.subList(0, size) : fetched;
    List<PowerUserDto> content = pageEntities.stream()
        .map(powerUserMapper::toDto)
        .toList();

    String nextCursor = null;
    Instant nextAfter = null;
    if (!pageEntities.isEmpty()) {
      PowerUser last = pageEntities.get(pageEntities.size() - 1);
      nextCursor = String.valueOf(last.getRank());
      nextAfter = last.getCreatedAt();
    }

    return new CursorPageResponsePowerUserDto(
        content,
        nextCursor,
        nextAfter,
        size,
        (long) content.size(),
        hasNext
    );
  }
}