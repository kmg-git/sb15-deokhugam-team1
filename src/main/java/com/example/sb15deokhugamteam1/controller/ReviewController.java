
package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.request.ReviewCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.ReviewSearchRequest;
import com.example.sb15deokhugamteam1.dto.request.ReviewUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.ReviewDto;
import com.example.sb15deokhugamteam1.dto.response.ReviewLikeDto;
import com.example.sb15deokhugamteam1.dto.response.ReviewPageResponse;
import com.example.sb15deokhugamteam1.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 목록 조회
    @GetMapping
    public ResponseEntity<ReviewPageResponse> search(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @ModelAttribute ReviewSearchRequest request
    ) {
        return ResponseEntity.ok(
                reviewService.search(request, requestUserId)
        );
    }

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<ReviewDto> create(
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return ResponseEntity.ok(
                reviewService.create(userId, request)
        );
    }

    // 리뷰 상세 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> get(
            @PathVariable UUID reviewId
    ) {
        return ResponseEntity.ok(reviewService.get(reviewId));
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> update(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(
                reviewService.update(reviewId, userId, request)
        );
    }

    // 리뷰 논리 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    ) {
        reviewService.delete(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    // 특정 도서의 리뷰 목록 조회
    @GetMapping("/books/{bookId}")
    public ResponseEntity<Page<ReviewDto>> getAllByBook(
            @PathVariable UUID bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                reviewService.getAllByBook(bookId, pageable)
        );
    }

    // 리뷰 좋아요 등록 및 취소
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ReviewLikeDto> toggleLike(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    ) {
        return ResponseEntity.ok(
                reviewService.toggleLike(reviewId, userId)
        );
    }
}
