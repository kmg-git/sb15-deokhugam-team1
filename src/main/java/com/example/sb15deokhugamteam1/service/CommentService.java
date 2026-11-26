package com.example.sb15deokhugamteam1.service;

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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentDto create(CommentCreateRequest request) {
        Review review = reviewRepository.findActiveByIdForUpdate(request.reviewId())
                .orElseThrow(() -> reviewNotFound(request.reviewId()));
        User author = userRepository.findByIdAndDeletedAtIsNull(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        Comment comment = commentRepository.saveAndFlush(new Comment(review, author, request.content()));
        review.increaseCommentCount();
        return commentMapper.toDto(comment);
    }

    @Transactional(readOnly = true)
    public CommentDto find(UUID commentId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        return commentMapper.toDto(comment);
    }

    @Transactional
    public CommentDto update(UUID commentId, UUID requesterId, CommentUpdateRequest request) {
        Comment comment = findActiveCommentForUpdate(commentId);
        verifyAuthor(comment, requesterId);
        comment.updateContent(request.content());
        commentRepository.flush();
        return commentMapper.toDto(comment);
    }

    @Transactional
    public void softDelete(UUID commentId, UUID requesterId) {
        Comment comment = findActiveCommentForUpdate(commentId);
        verifyAuthor(comment, requesterId);
        Review review = findReviewForUpdate(comment.getReview().getId());
        comment.softDelete();
        review.decreaseCommentCount();
    }

    @Transactional
    public void hardDelete(UUID commentId, UUID requesterId) {
        Comment comment = commentRepository.findByIdForUpdate(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        verifyAuthor(comment, requesterId);
        if (!comment.isDeleted()) {
            findReviewForUpdate(comment.getReview().getId()).decreaseCommentCount();
        }
        commentRepository.delete(comment);
    }

    private Comment findActiveCommentForUpdate(UUID commentId) {
        return commentRepository.findActiveByIdForUpdate(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    private void verifyAuthor(Comment comment, UUID requesterId) {
        if (!requesterId.equals(comment.getUser().getId())) {
            throw new CommentNotEqualUserException(requesterId);
        }
    }

    private Review findReviewForUpdate(UUID reviewId) {
        return reviewRepository.findByIdForUpdate(reviewId)
                .orElseThrow(() -> reviewNotFound(reviewId));
    }

    private NoSuchElementException reviewNotFound(UUID reviewId) {
        // TODO: 리뷰 담당 파트에서 전용 예외를 제공하면 교체한다.
        return new NoSuchElementException("Review not found: " + reviewId);
    }
}
