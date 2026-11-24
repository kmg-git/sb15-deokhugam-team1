package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.request.CommentUpdateRequest;
import com.example.sb15deokhugamteam1.entity.Comment;
import com.example.sb15deokhugamteam1.exception.comment.CommentNotEqualUserException;
import com.example.sb15deokhugamteam1.exception.comment.CommentNotFoundException;
import com.example.sb15deokhugamteam1.repository.CommentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    @Transactional
    public void updateComment(UUID commentId, UUID requesterId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if(!requesterId.equals(comment.getUser().getId())){
            throw new CommentNotEqualUserException(requesterId);
        }

        comment.updateContent(request.content());
    }
}
