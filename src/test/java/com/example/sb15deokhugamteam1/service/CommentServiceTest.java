package com.example.sb15deokhugamteam1.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.sb15deokhugamteam1.dto.request.CommentUpdateRequest;
import com.example.sb15deokhugamteam1.entity.Comment;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.exception.comment.CommentException;
import com.example.sb15deokhugamteam1.exception.comment.CommentNotEqualUserException;
import com.example.sb15deokhugamteam1.exception.comment.CommentNotFoundException;
import com.example.sb15deokhugamteam1.repository.CommentRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CommentServiceTest {
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final CommentService commentService = new CommentService(commentRepository);
    private final UUID commentId = UUID.randomUUID();
    private final UUID authorId = UUID.randomUUID();
    private final String originalContent = "Original comment";
    private final CommentUpdateRequest request = new CommentUpdateRequest("Updated comment");
    private Comment comment;

    @BeforeEach
    void setUp() {
        User author = mock(User.class);
        when(author.getId()).thenReturn(authorId);
        comment = new Comment(mock(Review.class), author, originalContent);
        when(commentRepository.findByIdAndDeletedAtIsNull(commentId)).thenReturn(Optional.of(comment));
    }

    @Test
    @DisplayName("작성자가 수정하면 댓글 내용이 변경된다")
    void 업데이트_성공_01() {
        // when
        commentService.updateComment(commentId, authorId, request);

        // then
        assertEquals(request.content(), comment.getContent());
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 수정하면 403 예외가 발생하고 댓글 내용이 유지된다")
    void 업데이트_실패_01() {
        // given
        UUID otherUserId = UUID.randomUUID();

        // when & then
        assertAll(
                () -> {
                    CommentNotEqualUserException exception = assertThrows(
                            CommentNotEqualUserException.class,
                            () -> commentService.updateComment(commentId, otherUserId, request));
                    assertEquals(HttpStatus.FORBIDDEN, exception.getErrorCode().getStatus());
                },
                () -> assertEquals(originalContent, comment.getContent())
        );
    }

    @Test
    @DisplayName("조회 가능한 댓글이 없으면 404 예외가 발생한다")
    void 업데이트_실패_02() {
        // given
        when(commentRepository.findByIdAndDeletedAtIsNull(commentId)).thenReturn(Optional.empty());

        // when & then
        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.updateComment(commentId, authorId, request));
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getStatus());
    }
}
