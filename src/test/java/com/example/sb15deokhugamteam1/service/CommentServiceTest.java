package com.example.sb15deokhugamteam1.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.sb15deokhugamteam1.dto.request.CommentCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.CommentUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.CommentDto;
import com.example.sb15deokhugamteam1.entity.Comment;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.exception.comment.CommentNotEqualUserException;
import com.example.sb15deokhugamteam1.exception.comment.CommentNotFoundException;
import com.example.sb15deokhugamteam1.exception.user.UserNotFoundException;
import com.example.sb15deokhugamteam1.mapper.dto.CommentMapper;
import com.example.sb15deokhugamteam1.repository.CommentRepository;
import com.example.sb15deokhugamteam1.repository.ReviewRepository;
import com.example.sb15deokhugamteam1.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

class CommentServiceTest {
    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CommentMapper commentMapper = mock(CommentMapper.class);
    private final CommentService commentService = new CommentService(
            commentRepository, reviewRepository, userRepository, commentMapper);
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
        when(commentRepository.findByIdForUpdate(commentId)).thenReturn(Optional.of(comment));
    }


    @Test
    @DisplayName("CommentCreateRequest를 받아와 댓글을 생성한다")
    void 크리에이트_성공_01() {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest createRequest = new CommentCreateRequest(reviewId, authorId, originalContent);
        Review review = comment.getReview();
        when(reviewRepository.findByIdForUpdate(reviewId)).thenReturn(Optional.of(review));
        CommentDto expected = new CommentDto(
                commentId, reviewId, authorId, "reader", originalContent, null, null);
        when(commentMapper.toDto(comment)).thenReturn(expected);
        User author = comment.getUser();
        when(userRepository.findByIdAndDeletedAtIsNull(authorId)).thenReturn(Optional.of(author));
        when(commentRepository.saveAndFlush(any(Comment.class))).thenReturn(comment);

        // when
        CommentDto result = commentService.create(createRequest);

        // then
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).saveAndFlush(captor.capture());
        Comment saved = captor.getValue();
        assertAll(
                () -> assertSame(review, saved.getReview()),
                () -> assertSame(author, saved.getUser()),
                () -> assertEquals(originalContent, saved.getContent()),
                () -> assertEquals(expected, result)
        );
        verify(review).increaseCommentCount();
    }

    @Test
    @DisplayName("리뷰 조회에 실패하여 404 예외가 발생한다")
    void 크리에이트_실패_01() {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest createRequest = new CommentCreateRequest(reviewId, authorId, originalContent);
        when(reviewRepository.findByIdForUpdate(reviewId)).thenReturn(Optional.empty());

        // when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> commentService.create(createRequest));

        // then
        assertEquals("Review not found: " + reviewId, exception.getMessage());
        verify(commentRepository, never()).saveAndFlush(any(Comment.class));
    }

    @Test
    @DisplayName("작성자 조회에 실패하여 404 예외가 발생한다")
    void 크리에이트_실패_02() {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest createRequest = new CommentCreateRequest(reviewId, authorId, originalContent);
        Review review = comment.getReview();
        when(reviewRepository.findByIdForUpdate(reviewId)).thenReturn(Optional.of(review));
        when(userRepository.findByIdAndDeletedAtIsNull(authorId)).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> commentService.create(createRequest));

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getStatus());
        verify(commentRepository, never()).saveAndFlush(any(Comment.class));
    }



    @Test
    @DisplayName("작성자가 수정하면 댓글 내용이 변경된다")
    void 업데이트_성공_01() {
        // when
        commentService.update(commentId, authorId, request);

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
                            () -> commentService.update(commentId, otherUserId, request));
                    assertEquals(HttpStatus.FORBIDDEN, exception.getErrorCode().getStatus());
                },
                () -> assertEquals(originalContent, comment.getContent())
        );
    }

    @Test
    @DisplayName("조회 가능한 댓글이 없으면 404 예외가 발생한다")
    void 업데이트_실패_02() {
        // given
        when(commentRepository.findByIdForUpdate(commentId)).thenReturn(Optional.empty());

        // when & then
        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.update(commentId, authorId, request));
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getStatus());
    }

    @Test
    @DisplayName("PK(UUID)를 기반으로 댓글을 찾는다")
    void 단건_조회_성공_01() {
        // given
        UUID reviewId = UUID.randomUUID();
        CommentDto expected = new CommentDto(
                commentId, reviewId, authorId, "reader", originalContent, null, null);
        when(commentMapper.toDto(comment)).thenReturn(expected);
        when(commentRepository.findByIdAndDeletedAtIsNull(commentId)).thenReturn(Optional.of(comment));

        // when
        CommentDto result = commentService.find(commentId);

        // then
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("댓글 단건 조회에 실패하여 404 예외가 발생한다")
    void 단건_조회_실패_01() {
        // given
        when(commentRepository.findByIdAndDeletedAtIsNull(commentId)).thenReturn(Optional.empty());

        // when
        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.find(commentId));

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getStatus());
    }

    @Test
    @DisplayName("논리 삭제를 성공했다")
    void 논리삭제_성공_01() {
        // given
        UUID reviewId = UUID.randomUUID();
        Review review = comment.getReview();
        when(reviewRepository.findByIdForUpdate(reviewId)).thenReturn(Optional.of(review));
        when(review.getId()).thenReturn(reviewId);

        // when
        commentService.softDelete(commentId, authorId);

        // then
        assertNotNull(comment.getDeletedAt());
        verify(commentRepository, never()).delete(comment);
        verify(review).decreaseCommentCount();
    }

    @Test
    @DisplayName("댓글 조회에 실패하여 404 예외가 발생한다")
    void 논리삭제_실패_01() {
        // given
        when(commentRepository.findByIdForUpdate(commentId)).thenReturn(Optional.empty());

        // when
        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.softDelete(commentId, authorId));

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getStatus());
    }

    @Test
    @DisplayName("작성자 작성자가 일치하지 않아 403 예외가 발생한다")
    void 논리삭제_실패_02() {
        // given
        UUID otherUserId = UUID.randomUUID();

        // when
        CommentNotEqualUserException exception = assertThrows(CommentNotEqualUserException.class,
                () -> commentService.softDelete(commentId, otherUserId));

        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.getErrorCode().getStatus());
        assertNull(comment.getDeletedAt());
        verify(commentRepository, never()).delete(comment);
    }

    @Test
    @DisplayName("물리 삭제를 성공했다")
    void 물리삭제_성공_01() {
        // given
        UUID reviewId = UUID.randomUUID();
        Review review = comment.getReview();
        when(reviewRepository.findByIdForUpdate(reviewId)).thenReturn(Optional.of(review));
        when(review.getId()).thenReturn(reviewId);

        // when
        commentService.hardDelete(commentId, authorId);

        // then
        verify(commentRepository).delete(comment);
        verify(review).decreaseCommentCount();
    }

    @Test
    @DisplayName("댓글 조회에 실패하여 404 예외가 발생한다")
    void 물리삭제_실패_01() {
        // given
        when(commentRepository.findByIdForUpdate(commentId)).thenReturn(Optional.empty());

        // when
        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.hardDelete(commentId, authorId));

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getStatus());
    }

    @Test
    @DisplayName("작성자 작성자가 일치하지 않아 403 예외가 발생한다")
    void 물리삭제_실패_02() {
        // given
        UUID otherUserId = UUID.randomUUID();

        // when
        CommentNotEqualUserException exception = assertThrows(CommentNotEqualUserException.class,
                () -> commentService.hardDelete(commentId, otherUserId));

        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.getErrorCode().getStatus());
        assertNull(comment.getDeletedAt());
        verify(commentRepository, never()).delete(comment);
    }

    @Test
    @DisplayName("논리 삭제한 댓글을 물리 삭제하면 리뷰 댓글 수는 한 번만 감소한다")
    void 논리삭제_후_물리삭제_시_리뷰와의_상호작용() {
        // given
        UUID reviewId = UUID.randomUUID();
        Review review = comment.getReview();
        when(review.getId()).thenReturn(reviewId);
        when(reviewRepository.findByIdForUpdate(reviewId)).thenReturn(Optional.of(review));

        // when
        commentService.softDelete(commentId, authorId);
        commentService.hardDelete(commentId, authorId);

        // then
        verify(review).decreaseCommentCount();
        verify(commentRepository).delete(comment);
    }
}
