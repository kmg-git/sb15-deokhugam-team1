package com.example.sb15deokhugamteam1.mapper.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.sb15deokhugamteam1.dto.response.CommentDto;
import com.example.sb15deokhugamteam1.entity.Comment;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.User;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

class CommentMapperTest {

  private final CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

  @Test
  void toDto_맵핑_성공() {
    // given
    UUID commentId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-11-24T01:00:00Z");
    Instant updatedAt = Instant.parse("2026-11-24T02:00:00Z");
    String content = "섬갈랐덴~";
    String nickname = "덴덴";

    Review review = mock(Review.class);
    when(review.getId()).thenReturn(reviewId);
    User user = new User("reader@example.com", nickname, "password");
    ReflectionTestUtils.setField(user, "id", userId);
    Comment comment = new Comment(review, user, content);
    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(comment, "createdAt", createdAt);
    ReflectionTestUtils.setField(comment, "updatedAt", updatedAt);

    // when
    CommentDto dto = mapper.toDto(comment);

    // then
    assertThat(dto).isEqualTo(
        new CommentDto(commentId, reviewId, userId, nickname, content, createdAt, updatedAt));
  }

  @Test
  void Null이면_Null을_돌려준다() {
    // given
    Comment comment = null;

    // when
    CommentDto dto = mapper.toDto(comment);

    // then
    assertThat(dto).isNull();
  }
}
