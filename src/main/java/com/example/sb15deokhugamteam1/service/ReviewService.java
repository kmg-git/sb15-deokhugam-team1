package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.request.ReviewUpdateRequest;
import com.example.sb15deokhugamteam1.dto.request.ReviewCreateRequest;
import com.example.sb15deokhugamteam1.dto.response.ReviewDto;
import com.example.sb15deokhugamteam1.dto.response.ReviewPageResponse;
import com.example.sb15deokhugamteam1.entity.Book;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.mapper.dto.ReviewMapper;
import com.example.sb15deokhugamteam1.repository.BookRepository;
import com.example.sb15deokhugamteam1.repository.ReviewQueryRepository;
import com.example.sb15deokhugamteam1.repository.ReviewRepository;
import com.example.sb15deokhugamteam1.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sb15deokhugamteam1.dto.request.ReviewSearchRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ReviewService  {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewQueryRepository reviewQueryRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookRepository bookRepository,
            UserRepository userRepository,
            ReviewQueryRepository reviewQueryRepository

    ) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.reviewQueryRepository = reviewQueryRepository;

    }

    // 리뷰 등록
    @Transactional
    public ReviewDto create(UUID userId, ReviewCreateRequest request) {

        //  삭제되지 않은 도서 조회
        Book book = bookRepository.findByIdAndDeletedAtIsNull(request.getBookId())
                .orElseThrow(() ->
                        new EntityNotFoundException("도서를 찾을 수 없습니다.")
                );

        // 삭제되지 않은 사용자 조회
        User user = userRepository.findById(userId)
                .filter(foundUser -> foundUser.getDeletedAt() == null)
                .orElseThrow(() ->
                        new EntityNotFoundException("사용자를 찾을 수 없습니다.")
                );

        //  리뷰 엔티티 생성
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

        //  도서 리뷰 통계 갱신
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

        //  삭제되지 않은 리뷰 조회
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() ->
                        new EntityNotFoundException("리뷰를 찾을 수 없습니다.")
                );

        //  작성자 계정 및 권한 확인
        User user = review.getUser();

        if (user.getDeletedAt() != null) {
            throw new IllegalStateException(
                    "삭제된 사용자는 리뷰를 수정할 수 없습니다."
            );
        }

        if (!user.getId().equals(userId)) {
            throw new IllegalStateException(
                    "리뷰를 수정할 권한이 없습니다."
            );
        }

        //  기존 별점 저장
        int oldRating = review.getRating();

        //  리뷰 내용 및 별점 수정
        review.update(
                request.getContent(),
                request.getRating()
        );

        //  응답 DTO 생성
        ReviewDto reviewDto = ReviewMapper.toDto(review);

        //  별점이 변경되었을 때만 도서 통계 갱신
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

        return reviewDto;
    }
    @Transactional
    public void delete(UUID reviewId,  UUID userId ) {

        // 삭제 되지 않은 리뷰 조회
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        User user = review.getUser();
        if (user.getDeletedAt() != null) {
            throw new IllegalStateException("삭제된 사용자 리뷰는 삭제할 수 없습니다.");

        }
        if (!user.getId().equals(userId)) {
            throw new IllegalStateException("삭제할 권한이 없습니다.");

        }
        int oldRating = review.getRating();
        UUID bookId = review.getBook().getId();

        // 리뷰 소프트 삭제
        review.softDelete();;

        int updatedCount = bookRepository.decreaseReview(
                bookId, oldRating
        );

        if (updatedCount != 1) {
            throw new IllegalStateException("도서 리뷰 통계 갱신에 실패했습니다.");

        }
    }
    @Transactional(readOnly = true)
    public ReviewDto get(UUID reviewId) {

        // 삭제 되지 않는 리뷰 조회
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 조회 결과물 DTO로 변환
        return ReviewMapper.toDto(review);
    }

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
    @Transactional(readOnly = true)
    public ReviewPageResponse search(ReviewSearchRequest request) {

        // 1. 조회 개수 설정 (기본값 50)
        int limit = request.getLimit() == null ? 50 : request.getLimit();

        if (limit <= 0) {
            throw new IllegalArgumentException("조회 개수는 1 이상이어야 합니다.");
        }

        // 2. 검색 조건에 맞는 리뷰 조회
        List<Review> reviews = reviewQueryRepository.searchReviews(
                request.getUserId(),
                request.getBookId(),
                request.getKeyword(),
                request.getOrderBy(),
                request.getDirection(),
                request.getCursor(),
                request.getAfter(),
                limit
        );

        // 3. 다음 페이지 존재 여부 확인
        boolean hasNext = reviews.size() > limit;

        // 4. limit보다 하나 더 조회한 마지막 데이터 제거
        if (hasNext) {
            reviews.remove(reviews.size() - 1);
        }

        // 5. 다음 페이지 커서 생성
        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext && !reviews.isEmpty()) {

            Review lastReview = reviews.get(reviews.size() - 1);

            String orderBy = request.getOrderBy() == null
                    ? "createdAt"
                    : request.getOrderBy();

            if ("rating".equals(orderBy)) {

                nextCursor = lastReview.getRating()
                        + "|" + lastReview.getCreatedAt()
                        + "|" + lastReview.getId();

            } else if ("createdAt".equals(orderBy)) {

                nextCursor = lastReview.getCreatedAt()
                        + "|" + lastReview.getId();

            } else {
                throw new IllegalArgumentException(
                        "지원하지 않는 정렬 기준입니다."
                );
            }

            nextAfter = lastReview.getCreatedAt();
        }

        // 6. Review 엔티티를 DTO로 변환
        List<ReviewDto> content = reviews.stream()
                .map(ReviewMapper::toDto)
                .toList();

        // 7. 현재 페이지의 실제 리뷰 개수
        int size = content.size();

        // 8. 전체 검색 결과 개수 조회
        long totalElements = reviewQueryRepository.countReviews(
                request.getUserId(),
                request.getBookId(),
                request.getKeyword()
        );

        // 9. 페이지 응답 반환
        return new ReviewPageResponse(
                content,
                nextCursor,
                nextAfter,
                size,
                totalElements,
                hasNext
        );
    }
}
