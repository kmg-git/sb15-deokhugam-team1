package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponseCommentDto;
import com.example.sb15deokhugamteam1.dto.request.CommentCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.CommentUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.CommentDto;
import com.example.sb15deokhugamteam1.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {
    private static final String REQUEST_USER_ID = "Deokhugam-Request-User-ID";

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<CursorPageResponseCommentDto> getCommentsByReviewId(
            @RequestParam("reviewId") UUID reviewId,
            @RequestParam(name = "direction", defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "after", required = false) Instant after,
            @RequestParam(name = "limit", defaultValue = "50") @Positive int limit
    ) {
        // TODO: 나중에 커서 페이지 구현
        return null;
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(request));
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable("commentId") UUID commentId) {
        return commentService.find(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(
            @PathVariable("commentId") UUID commentId,
            @RequestHeader(REQUEST_USER_ID) UUID requesterId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        return commentService.update(commentId, requesterId, request);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("commentId") UUID commentId,
            @RequestHeader(REQUEST_USER_ID) UUID requesterId
    ) {
        commentService.softDelete(commentId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> permanentDeleteComment(
            @PathVariable("commentId") UUID commentId,
            @RequestHeader(REQUEST_USER_ID) UUID requesterId
    ) {
        commentService.hardDelete(commentId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
