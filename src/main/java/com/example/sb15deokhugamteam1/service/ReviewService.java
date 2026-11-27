
package com.example.sb15deokhugamteam1.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.sb15deokhugamteam1.dto.request.ReviewCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.ReviewSearchRequest;
import com.example.sb15deokhugamteam1.dto.request.ReviewUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.ReviewDto;
import com.example.sb15deokhugamteam1.dto.response.ReviewPageResponse;
import com.example.sb15deokhugamteam1.dto.response.ReviewLikeDto;
import com.example.sb15deokhugamteam1.entity.Book;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.ReviewLike;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.exception.review.ReviewForbiddenException;
import com.example.sb15deokhugamteam1.exception.review.ReviewNotFoundException;
import com.example.sb15deokhugamteam1.mapper.dto.ReviewMapper;
import com.example.sb15deokhugamteam1.repository.BookRepository;
import com.example.sb15deokhugamteam1.repository.ReviewLikeRepository;
import com.example.sb15deokhugamteam1.repository.ReviewQueryRepository;
import com.example.sb15deokhugamteam1.repository.ReviewRepository;
import com.example.sb15deokhugamteam1.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookRepository bookRepository,
            UserRepository userRepository,
            ReviewQueryRepository reviewQueryRepository,
            ReviewLikeRepository reviewLikeRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.reviewQueryRepository = reviewQueryRepository;
        this.reviewLikeRepository = reviewLikeRepository;
    }

    // 리뷰 등록
    @Transactional
    public ReviewDto create(UUID userId, ReviewCreateRequest request) {

        // 삭제되지 않은 사용자 조회
        User user = userRepository.findById(userId)
                .filter(foundUser -> foundUser.getDeletedAt() == null)
                .orElseThrow(() ->
                        new EntityNotFoundException("사용자를 찾을 수 없습니다.")
                );

        // 삭제되지 않은 도서 조회
        Book book = bookRepository.findByIdAndDeletedAtIsNull(request.getBookId())
                .orElseThrow(() ->
                        new EntityNotFoundException("도서를 찾을 수 없습니다.")
                );

        // 동일 사용자의 동일 도서 리뷰 중복 확인
        // 논리 삭제된 리뷰도 중복으로 취급
        if (reviewRepository.existsByBook_IdAndUser_Id(
                book.getId(), user.getId())) {
            throw new IllegalStateException(
                    "이미 해당 도서에 작성한 리뷰가 있습니다."
            );
        }

        // 리뷰 엔티티 생성
        Review review = new Review(
                book,
                user,
                request.getContent(),
                request.getRating()
        );

        // 리뷰 저장 및 DB 반영
        Review savedReview = reviewRepository.saveAndFlush(review);

        // 응답 DTO 생성
        ReviewDto reviewDto = ReviewMapper.toDto(savedReview);

        // 도서 리뷰 통계 갱신
        int updatedCount = bookRepository.increaseReview(
                book.getId(),
                request.getRating()
        );

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "도서 리뷰 통계 갱신에 실패했습니다."
            );
        }

        return reviewDto;
    }

    // 리뷰 수정
    @Transactional
    public ReviewDto update(
            UUID reviewId,
            UUID userId,
            ReviewUpdateRequest request
    ) {

        // 삭제되지 않은 리뷰 조회
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        // 수정할 내용 확인
        if (request.getContent() == null && request.getRating() == null) {
            throw new IllegalArgumentException(
                    "수정할 내용이나 별점을 하나 이상 입력해야 합니다."
            );
        }

        // 작성자 계정 및 권한 확인
        User user = review.getUser();

        if (user.getDeletedAt() != null) {
            throw new ReviewForbiddenException(reviewId);
        }

        if (!user.getId().equals(userId)) {
            throw new ReviewForbiddenException(reviewId);
        }

        // 기존 별점 저장
        int oldRating = review.getRating();

        // 리뷰 내용 및 별점 수정
        review.update(
                request.getContent(),
                request.getRating()
        );

        // 별점이 변경되었을 때만 도서 통계 갱신
        if (request.getRating() != null
                && oldRating != request.getRating()) {

            int diff = request.getRating() - oldRating;

            int updatedCount = bookRepository.changeReviewRating(
                    review.getBook().getId(),
                    diff
            );

            if (updatedCount != 1) {
                throw new IllegalStateException(
                        "도서 별점 통계 갱신에 실패했습니다."
                );
            }
        }

        // 요청자의 좋아요 여부 확인
        boolean likedByMe = reviewLikeRepository
                .findByReview_IdAndUser_Id(reviewId, userId)
                .isPresent();

        return ReviewMapper.toDto(review, likedByMe);
    }

    // 리뷰 논리 삭제
    @Transactional
    public void delete(UUID reviewId, UUID userId) {

        // 삭제되지 않은 리뷰 조회
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        User user = review.getUser();

        // 삭제된 사용자 확인
        if (user.getDeletedAt() != null) {
            throw new ReviewForbiddenException(reviewId);
        }

        // 작성자 권한 확인
        if (!user.getId().equals(userId)) {
            throw new ReviewForbiddenException(reviewId);
        }

        int oldRating = review.getRating();
        UUID bookId = review.getBook().getId();

        // 리뷰 논리 삭제
        review.softDelete();

        // 도서 리뷰 통계 갱신
        int updatedCount = bookRepository.decreaseReview(
                bookId,
                oldRating
        );

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "도서 리뷰 통계 갱신에 실패했습니다."
            );
        }
    }

    // 리뷰 물리 삭제
    @Transactional
    public void hardDelete(UUID reviewId, UUID userId) {

        // 논리 삭제 여부와 관계없이 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        // 작성자 권한 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewForbiddenException(reviewId);
        }

        // 논리 삭제되지 않은 리뷰만 도서 통계 감소
        if (!review.isDeleted()) {

            int updatedCount = bookRepository.decreaseReview(
                    review.getBook().getId(),
                    review.getRating()
            );

            if (updatedCount != 1) {
                throw new IllegalStateException(
                        "도서 리뷰 통계 갱신에 실패했습니다."
                );
            }
        }

        // 리뷰에 연결된 좋아요 먼저 삭제
        reviewLikeRepository.deleteAllByReviewId(reviewId);

        // 리뷰 물리 삭제
        reviewRepository.delete(review);
    }

    // 리뷰 단건 조회
    @Transactional(readOnly = true)
    public ReviewDto get(UUID reviewId, UUID userId) {

        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        // 요청자가 해당 리뷰에 좋아요를 눌렀는지 확인
        boolean likedByMe = reviewLikeRepository
                .findByReview_IdAndUser_Id(reviewId, userId)
                .isPresent();

        return ReviewMapper.toDto(review, likedByMe);
    }

    // 도서별 리뷰 조회
    @Transactional(readOnly = true)
    public Page<ReviewDto> getAllByBook(
            UUID bookId,
            Pageable pageable
    ) {
        Page<Review> reviews =
                reviewRepository.findAllByBook_IdAndDeletedAtIsNull(
                        bookId,
                        pageable
                );

        return reviews.map(ReviewMapper::toDto);
    }

    // 리뷰 검색 (요청자 정보 없이 조회)
    @Transactional(readOnly = true)
    public ReviewPageResponse search(ReviewSearchRequest request) {
        return search(request, null);
    }

    // 요청자의 좋아요 여부를 포함한 리뷰 검색
    @Transactional(readOnly = true)
    public ReviewPageResponse search(
            ReviewSearchRequest request,
            UUID requestUserId
    ) {
        int limit = request.getLimit() == null ? 50 : request.getLimit();

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "조회 개수는 1 이상이어야 합니다."
            );
        }

        String orderBy = request.getOrderBy();

        if (orderBy == null || orderBy.isBlank()) {
            orderBy = "createdAt";
        }

        if (!orderBy.equals("createdAt") && !orderBy.equals("rating")) {
            throw new IllegalArgumentException(
                    "지원하지 않는 정렬 기준입니다."
            );
        }

        String direction = request.getDirection();

        if (direction == null || direction.isBlank()) {
            direction = "DESC";
        }

        direction = direction.toUpperCase(java.util.Locale.ROOT);

        if (!direction.equals("ASC") && !direction.equals("DESC")) {
            throw new IllegalArgumentException(
                    "지원하지 않는 정렬 방향입니다."
            );
        }

        List<Review> reviews = reviewQueryRepository.searchReviews(
                request.getUserId(),
                request.getBookId(),
                request.getKeyword(),
                orderBy,
                direction,
                request.getCursor(),
                request.getAfter(),
                limit
        );

        boolean hasNext = reviews.size() > limit;

        if (hasNext) {
            reviews.remove(reviews.size() - 1);
        }

        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext && !reviews.isEmpty()) {
            Review lastReview = reviews.get(reviews.size() - 1);

            if (orderBy.equals("rating")) {
                nextCursor = lastReview.getRating()
                        + "|" + lastReview.getCreatedAt()
                        + "|" + lastReview.getId();
            } else {
                nextCursor = lastReview.getCreatedAt()
                        + "|" + lastReview.getId();
            }

            nextAfter = lastReview.getCreatedAt();
        }

        Set<UUID> reviewIds = reviews.stream()
                .map(Review::getId)
                .collect(Collectors.toSet());

        Set<UUID> likedReviewIds;

        if (requestUserId != null && !reviewIds.isEmpty()) {
            likedReviewIds = reviewLikeRepository
                    .findAllByUser_IdAndReview_IdIn(requestUserId, reviewIds)
                    .stream()
                    .map(reviewLike -> reviewLike.getReview().getId())
                    .collect(Collectors.toSet());
        } else {
            likedReviewIds = Set.of();
        }

        List<ReviewDto> content = reviews.stream()
                .map(review -> ReviewMapper.toDto(
                        review,
                        likedReviewIds.contains(review.getId())
                ))
                .toList();

        long totalElements = reviewQueryRepository.countReviews(
                request.getUserId(),
                request.getBookId(),
                request.getKeyword()
        );

        return new ReviewPageResponse(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                totalElements,
                hasNext
        );
    }

    // 좋아요 등록 및 취소
    @Transactional
    public ReviewLikeDto toggleLike(UUID reviewId, UUID userId) {

        // 삭제되지 않은 리뷰 조회 및 잠금
        Review review = reviewRepository
                .findByIdAndDeletedAtIsNullForUpdate(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        // 삭제되지 않은 사용자 조회
        User user = userRepository.findById(userId)
                .filter(foundUser -> foundUser.getDeletedAt() == null)
                .orElseThrow(() ->
                        new EntityNotFoundException("사용자를 찾을 수 없습니다.")
                );

        // 기존 좋아요 여부 확인
        var existingLike = reviewLikeRepository
                .findByReview_IdAndUser_Id(reviewId, userId);

        boolean liked;

        if (existingLike.isPresent()) {

            // 이미 좋아요를 눌렀다면 취소
            reviewLikeRepository.delete(existingLike.get());
            review.decreaseLikeCount();
            liked = false;

        } else {

            // 좋아요가 없다면 등록
            ReviewLike reviewLike = new ReviewLike(review, user);
            reviewLikeRepository.save(reviewLike);
            review.increaseLikeCount();
            liked = true;
        }

        return new ReviewLikeDto(reviewId, userId, liked);
    }
}
