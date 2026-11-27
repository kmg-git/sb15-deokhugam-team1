
package com.example.sb15deokhugamteam1.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.example.sb15deokhugamteam1.dto.request.ReviewCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.ReviewSearchRequest;
import com.example.sb15deokhugamteam1.dto.request.ReviewUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.ReviewDto;
import com.example.sb15deokhugamteam1.dto.response.ReviewPageResponse;
import com.example.sb15deokhugamteam1.entity.Book;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.ReviewLike;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.exception.review.ReviewForbiddenException;
import com.example.sb15deokhugamteam1.exception.review.ReviewNotFoundException;
import com.example.sb15deokhugamteam1.repository.BookRepository;
import com.example.sb15deokhugamteam1.repository.ReviewLikeRepository;
import com.example.sb15deokhugamteam1.repository.ReviewQueryRepository;
import com.example.sb15deokhugamteam1.repository.ReviewRepository;
import com.example.sb15deokhugamteam1.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewQueryRepository reviewQueryRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Book createBook() {
        return new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );
    }

    private User createUser() {
        return new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );
    }

    private Review createSearchReview(int rating, Instant createdAt) {
        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        Review review = new Review(
                book,
                user,
                "페이지네이션 테스트 리뷰",
                rating
        );

        ReflectionTestUtils.setField(review, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(review, "createdAt", createdAt);

        return review;
    }

    // 리뷰 생성

    @Test
    void create_도서가_존재하지_않으면_예외가_발생한다() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User user = createUser();
        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(request.getBookId()).thenReturn(bookId);

        when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.create(userId, request)
        );

        verify(userRepository).findById(userId);
        verify(bookRepository).findByIdAndDeletedAtIsNull(bookId);
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_사용자가_존재하지_않으면_예외가_발생한다() {
        UUID userId = UUID.randomUUID();

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.create(userId, request)
        );

        verify(userRepository).findById(userId);
        verifyNoInteractions(bookRepository);
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_삭제된_사용자는_리뷰를_등록할_수_없다() {
        UUID userId = UUID.randomUUID();

        User user = createUser();
        user.softDelete();

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.create(userId, request)
        );

        verify(userRepository).findById(userId);
        verifyNoInteractions(bookRepository);
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_정상적으로_리뷰를_등록한다() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(book, "id", bookId);
        ReflectionTestUtils.setField(user, "id", userId);

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(request.getBookId()).thenReturn(bookId);
        when(request.getContent()).thenReturn("정말 재미있는 책입니다.");
        when(request.getRating()).thenReturn(5);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .thenReturn(Optional.of(book));

        when(reviewRepository.existsByBook_IdAndUser_Id(bookId, userId))
                .thenReturn(false);

        when(reviewRepository.saveAndFlush(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookRepository.increaseReview(bookId, 5))
                .thenReturn(1);

        ReviewDto result = reviewService.create(userId, request);

        assertNotNull(result);
        assertEquals("정말 재미있는 책입니다.", result.getContent());
        assertEquals(5, result.getRating());

        verify(reviewRepository).existsByBook_IdAndUser_Id(bookId, userId);
        verify(reviewRepository).saveAndFlush(any(Review.class));
        verify(bookRepository).increaseReview(bookId, 5);
    }

    @Test
    void create_이미_리뷰를_작성한_도서면_예외가_발생한다() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(book, "id", bookId);
        ReflectionTestUtils.setField(user, "id", userId);

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(request.getBookId()).thenReturn(bookId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .thenReturn(Optional.of(book));

        when(reviewRepository.existsByBook_IdAndUser_Id(bookId, userId))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> reviewService.create(userId, request)
        );

        verify(reviewRepository, never()).saveAndFlush(any());
        verify(bookRepository, never()).increaseReview(any(), anyInt());
    }

    @Test
    void create_도서_통계_갱신에_실패하면_예외가_발생한다() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(book, "id", bookId);
        ReflectionTestUtils.setField(user, "id", userId);

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(request.getBookId()).thenReturn(bookId);
        when(request.getContent()).thenReturn("테스트 리뷰");
        when(request.getRating()).thenReturn(5);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .thenReturn(Optional.of(book));

        when(reviewRepository.existsByBook_IdAndUser_Id(bookId, userId))
                .thenReturn(false);

        when(reviewRepository.saveAndFlush(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookRepository.increaseReview(bookId, 5))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> reviewService.create(userId, request)
        );

        verify(reviewRepository).saveAndFlush(any(Review.class));
        verify(bookRepository).increaseReview(bookId, 5);
    }

    // 리뷰 수정

    @Test
    void update_존재하지_않는_리뷰면_예외가_발생한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.update(reviewId, userId, request)
        );

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
        verifyNoInteractions(reviewLikeRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void update_삭제된_사용자는_리뷰를_수정할_수_없다() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();
        user.softDelete();

        ReflectionTestUtils.setField(user, "id", userId);

        Review review = new Review(book, user, "기존 리뷰", 4);
        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        // 수정 요청이 비어 있지 않도록 설정
        when(request.getContent()).thenReturn("수정된 리뷰");

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ReviewForbiddenException.class,
                () -> reviewService.update(reviewId, userId, request)
        );

        verify(bookRepository, never()).changeReviewRating(any(), anyInt());
        verifyNoInteractions(reviewLikeRepository);
    }

    @Test
    void update_작성자가_아닌_사용자는_리뷰를_수정할_수_없다() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User owner = createUser();

        ReflectionTestUtils.setField(owner, "id", ownerId);

        Review review = new Review(book, owner, "기존 리뷰", 4);
        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        // 수정 요청이 비어 있지 않도록 설정
        when(request.getContent()).thenReturn("수정된 리뷰");

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ReviewForbiddenException.class,
                () -> reviewService.update(reviewId, otherUserId, request)
        );

        verify(bookRepository, never()).changeReviewRating(any(), anyInt());
        verifyNoInteractions(reviewLikeRepository);
    }

    @Test
    void update_정상적으로_리뷰를_수정한다() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);
        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(request.getContent()).thenReturn("수정된 리뷰");
        when(request.getRating()).thenReturn(5);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(bookRepository.changeReviewRating(book.getId(), 2))
                .thenReturn(1);

        when(reviewLikeRepository.findByReview_IdAndUser_Id(reviewId, userId))
                .thenReturn(Optional.empty());

        ReviewDto result = reviewService.update(reviewId, userId, request);

        assertNotNull(result);
        assertEquals("수정된 리뷰", result.getContent());
        assertEquals(5, result.getRating());
        assertFalse(result.isLikedByMe());

        verify(bookRepository).changeReviewRating(book.getId(), 2);
        verify(reviewLikeRepository).findByReview_IdAndUser_Id(reviewId, userId);
    }

    @Test
    void update_별점이_변경되지_않으면_도서_통계를_갱신하지_않는다() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);
        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(request.getContent()).thenReturn("수정된 리뷰");
        when(request.getRating()).thenReturn(3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewLikeRepository.findByReview_IdAndUser_Id(reviewId, userId))
                .thenReturn(Optional.empty());

        ReviewDto result = reviewService.update(reviewId, userId, request);

        assertNotNull(result);
        assertEquals("수정된 리뷰", result.getContent());
        assertEquals(3, result.getRating());

        verify(bookRepository, never()).changeReviewRating(any(), anyInt());
        verify(reviewLikeRepository).findByReview_IdAndUser_Id(reviewId, userId);
    }

    @Test
    void update_별점이_null이면_기존_별점을_유지한다() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);
        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(request.getContent()).thenReturn("내용만 수정");
        when(request.getRating()).thenReturn(null);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewLikeRepository.findByReview_IdAndUser_Id(reviewId, userId))
                .thenReturn(Optional.empty());

        ReviewDto result = reviewService.update(reviewId, userId, request);

        assertNotNull(result);
        assertEquals("내용만 수정", result.getContent());
        assertEquals(3, result.getRating());

        verify(bookRepository, never()).changeReviewRating(any(), anyInt());
        verify(reviewLikeRepository).findByReview_IdAndUser_Id(reviewId, userId);
    }

    @Test
    void update_수정할_내용과_별점이_모두_null이면_예외가_발생한다() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(user, "id", userId);

        Review review = new Review(book, user, "기존 리뷰", 3);
        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(request.getContent()).thenReturn(null);
        when(request.getRating()).thenReturn(null);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.update(reviewId, userId, request)
        );

        verifyNoInteractions(reviewLikeRepository);
        verify(bookRepository, never()).changeReviewRating(any(), anyInt());
    }

    @Test
    void update_도서_통계_갱신에_실패하면_예외가_발생한다() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);
        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(request.getContent()).thenReturn("수정된 리뷰");
        when(request.getRating()).thenReturn(5);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(bookRepository.changeReviewRating(book.getId(), 2))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> reviewService.update(reviewId, userId, request)
        );

        verify(bookRepository).changeReviewRating(book.getId(), 2);
        verifyNoInteractions(reviewLikeRepository);
    }

    // 리뷰 논리 삭제

    @Test
    void delete_존재하지_않는_리뷰면_예외가_발생한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.delete(reviewId, userId)
        );

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
        verify(bookRepository, never()).decreaseReview(any(), anyInt());
    }

    @Test
    void delete_삭제된_사용자는_리뷰를_삭제할_수_없다() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();
        user.softDelete();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ReviewForbiddenException.class,
                () -> reviewService.delete(reviewId, userId)
        );

        verify(bookRepository, never()).decreaseReview(any(), anyInt());
    }

    @Test
    void delete_작성자가_아닌_사용자는_리뷰를_삭제할_수_없다() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User owner = createUser();

        ReflectionTestUtils.setField(owner, "id", ownerId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, owner, "기존 리뷰", 3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        assertThrows(
                ReviewForbiddenException.class,
                () -> reviewService.delete(reviewId, otherUserId)
        );

        verify(bookRepository, never()).decreaseReview(any(), anyInt());
    }

    @Test
    void delete_정상적으로_리뷰를_삭제한다() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(bookRepository.decreaseReview(book.getId(), 3))
                .thenReturn(1);

        reviewService.delete(reviewId, userId);

        assertTrue(review.isDeleted());

        verify(bookRepository).decreaseReview(book.getId(), 3);
    }

    @Test
    void delete_도서_통계_갱신에_실패하면_예외가_발생한다() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(bookRepository.decreaseReview(book.getId(), 3))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> reviewService.delete(reviewId, userId)
        );

        verify(bookRepository).decreaseReview(book.getId(), 3);
    }

    // 리뷰 조회

    @Test
    void get_존재하지_않는_리뷰면_예외가_발생한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.get(reviewId, userId)
        );

        verifyNoInteractions(reviewLikeRepository);
    }

    @Test
    void get_정상적으로_리뷰를_조회한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "조회할 리뷰", 4);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewLikeRepository.findByReview_IdAndUser_Id(reviewId, userId))
                .thenReturn(Optional.empty());

        ReviewDto result = reviewService.get(reviewId, userId);

        assertNotNull(result);
        assertEquals("조회할 리뷰", result.getContent());
        assertEquals(4, result.getRating());
        assertFalse(result.isLikedByMe());

        verify(reviewLikeRepository).findByReview_IdAndUser_Id(reviewId, userId);
    }

    @Test
    void get_내가_좋아요한_리뷰면_true를_반환한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "좋아요한 리뷰", 5);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewLikeRepository.findByReview_IdAndUser_Id(reviewId, userId))
                .thenReturn(Optional.of(mock(ReviewLike.class)));

        ReviewDto result = reviewService.get(reviewId, userId);

        assertNotNull(result);
        assertTrue(result.isLikedByMe());
    }

    @Test
    void getAllByBook_정상적으로_리뷰_목록을_조회한다() {
        UUID bookId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        ReflectionTestUtils.setField(book, "id", bookId);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        Review review1 = new Review(book, user, "첫 번째 리뷰", 4);
        Review review2 = new Review(book, user, "두 번째 리뷰", 5);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Review> reviewPage = new PageImpl<>(
                List.of(review1, review2),
                pageable,
                2
        );

        when(reviewRepository.findAllByBook_IdAndDeletedAtIsNull(bookId, pageable))
                .thenReturn(reviewPage);

        Page<ReviewDto> result = reviewService.getAllByBook(bookId, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("첫 번째 리뷰", result.getContent().get(0).getContent());
        assertEquals("두 번째 리뷰", result.getContent().get(1).getContent());
    }

    // 리뷰 검색

    @Test
    void search_정상적으로_리뷰를_검색한다() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        ReviewSearchRequest request = new ReviewSearchRequest();
        ReflectionTestUtils.setField(request, "userId", userId);
        ReflectionTestUtils.setField(request, "bookId", bookId);
        ReflectionTestUtils.setField(request, "keyword", "테스트");
        ReflectionTestUtils.setField(request, "limit", 10);

        List<Review> reviews = new ArrayList<>();

        when(reviewQueryRepository.searchReviews(
                userId, bookId, "테스트",
                "createdAt", "DESC", null, null, 10
        )).thenReturn(reviews);

        when(reviewQueryRepository.countReviews(userId, bookId, "테스트"))
                .thenReturn(0L);

        ReviewPageResponse response = reviewService.search(request);

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.size());
        assertEquals(0L, response.totalElements());
        assertFalse(response.hasNext());
        assertNull(response.nextCursor());
        assertNull(response.nextAfter());
    }

    @Test
    void search_limit이_null이면_기본값_50을_사용한다() {
        ReviewSearchRequest request = new ReviewSearchRequest();

        List<Review> reviews = new ArrayList<>();

        when(reviewQueryRepository.searchReviews(
                null, null, null,
                "createdAt", "DESC", null, null, 50
        )).thenReturn(reviews);

        when(reviewQueryRepository.countReviews(null, null, null))
                .thenReturn(0L);

        ReviewPageResponse response = reviewService.search(request);

        assertEquals(0, response.size());

        verify(reviewQueryRepository).searchReviews(
                null, null, null,
                "createdAt", "DESC", null, null, 50
        );
    }

    @Test
    void search_limit이_0이면_예외가_발생한다() {
        ReviewSearchRequest request = new ReviewSearchRequest();
        ReflectionTestUtils.setField(request, "limit", 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.search(request)
        );

        verifyNoInteractions(reviewQueryRepository);
    }

    @Test
    void search_createdAt_정렬에서_다음_페이지_커서를_생성한다() {
        ReviewSearchRequest request = new ReviewSearchRequest();

        ReflectionTestUtils.setField(request, "limit", 2);
        ReflectionTestUtils.setField(request, "orderBy", "createdAt");
        ReflectionTestUtils.setField(request, "direction", "DESC");

        Review first = createSearchReview(
                5, Instant.parse("2026-11-20T10:00:00Z")
        );

        Review second = createSearchReview(
                4, Instant.parse("2026-11-21T10:00:00Z")
        );

        Review third = createSearchReview(
                3, Instant.parse("2026-11-22T10:00:00Z")
        );

        List<Review> reviews = new ArrayList<>(
                List.of(first, second, third)
        );

        when(reviewQueryRepository.searchReviews(
                null, null, null,
                "createdAt", "DESC", null, null, 2
        )).thenReturn(reviews);

        when(reviewQueryRepository.countReviews(null, null, null))
                .thenReturn(3L);

        ReviewPageResponse response = reviewService.search(request);

        assertTrue(response.hasNext());
        assertEquals(2, response.size());
        assertEquals(2, response.content().size());

        assertEquals(
                second.getCreatedAt() + "|" + second.getId(),
                response.nextCursor()
        );

        assertEquals(second.getCreatedAt(), response.nextAfter());
    }

    @Test
    void search_로그인한_사용자의_좋아요_여부를_반영한다() {
        UUID requestUserId = UUID.randomUUID();

        ReviewSearchRequest request = new ReviewSearchRequest();
        ReflectionTestUtils.setField(request, "limit", 10);

        Review review = createSearchReview(
                5, Instant.parse("2026-11-20T10:00:00Z")
        );

        ReviewLike reviewLike = new ReviewLike(
                review,
                createUser()
        );

        List<Review> reviews = new ArrayList<>();
        reviews.add(review);

        when(reviewQueryRepository.searchReviews(
                null, null, null,
                "createdAt", "DESC", null, null, 10
        )).thenReturn(reviews);

        when(reviewLikeRepository.findAllByUser_IdAndReview_IdIn(
                requestUserId,
                Set.of(review.getId())
        )).thenReturn(List.of(reviewLike));

        when(reviewQueryRepository.countReviews(null, null, null))
                .thenReturn(1L);

        ReviewPageResponse response =
                reviewService.search(request, requestUserId);

        assertEquals(1, response.size());
        assertTrue(response.content().get(0).isLikedByMe());
    }

    @Test
    void search_비로그인_사용자는_좋아요를_조회하지_않는다() {
        ReviewSearchRequest request = new ReviewSearchRequest();
        ReflectionTestUtils.setField(request, "limit", 10);

        Review review = createSearchReview(
                5, Instant.parse("2026-11-20T10:00:00Z")
        );

        List<Review> reviews = new ArrayList<>();
        reviews.add(review);

        when(reviewQueryRepository.searchReviews(
                null, null, null,
                "createdAt", "DESC", null, null, 10
        )).thenReturn(reviews);

        when(reviewQueryRepository.countReviews(null, null, null))
                .thenReturn(1L);

        ReviewPageResponse response = reviewService.search(request);

        assertEquals(1, response.size());
        assertFalse(response.content().get(0).isLikedByMe());

        verify(reviewLikeRepository, never())
                .findAllByUser_IdAndReview_IdIn(any(UUID.class), anySet());
    }

    @Test
    void search_조회_결과가_limit보다_적으면_다음_페이지가_없다() {
        ReviewSearchRequest request = new ReviewSearchRequest();
        ReflectionTestUtils.setField(request, "limit", 2);

        List<Review> reviews = new ArrayList<>();
        reviews.add(createSearchReview(
                5, Instant.parse("2026-11-20T10:00:00Z")
        ));

        when(reviewQueryRepository.searchReviews(
                null, null, null,
                "createdAt", "DESC", null, null, 2
        )).thenReturn(reviews);

        when(reviewQueryRepository.countReviews(null, null, null))
                .thenReturn(1L);

        ReviewPageResponse response = reviewService.search(request);

        assertFalse(response.hasNext());
        assertEquals(1, response.size());
        assertNull(response.nextCursor());
        assertNull(response.nextAfter());
    }

    @Test
    void search_지원하지_않는_정렬_기준이면_예외가_발생한다() {
        ReviewSearchRequest request = new ReviewSearchRequest();

        ReflectionTestUtils.setField(request, "limit", 1);
        ReflectionTestUtils.setField(request, "orderBy", "unknown");

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.search(request)
        );

        verifyNoInteractions(reviewQueryRepository);
    }

    @Test
    void search_검색_결과가_비어_있으면_좋아요를_조회하지_않는다() {
        UUID requestUserId = UUID.randomUUID();

        ReviewSearchRequest request = new ReviewSearchRequest();
        ReflectionTestUtils.setField(request, "limit", 10);

        when(reviewQueryRepository.searchReviews(
                null, null, null,
                "createdAt", "DESC", null, null, 10
        )).thenReturn(new ArrayList<>());

        when(reviewQueryRepository.countReviews(null, null, null))
                .thenReturn(0L);

        ReviewPageResponse response =
                reviewService.search(request, requestUserId);

        assertTrue(response.content().isEmpty());
        assertEquals(0, response.size());
        assertFalse(response.hasNext());

        verify(reviewLikeRepository, never())
                .findAllByUser_IdAndReview_IdIn(any(UUID.class), anySet());
    }

    @Test
    void search_정렬_방향이_소문자여도_정상적으로_검색한다() {
        ReviewSearchRequest request = new ReviewSearchRequest();

        ReflectionTestUtils.setField(request, "limit", 10);
        ReflectionTestUtils.setField(request, "direction", "desc");

        when(reviewQueryRepository.searchReviews(
                null, null, null,
                "createdAt", "DESC", null, null, 10
        )).thenReturn(new ArrayList<>());

        when(reviewQueryRepository.countReviews(null, null, null))
                .thenReturn(0L);

        ReviewPageResponse response = reviewService.search(request);

        assertNotNull(response);

        verify(reviewQueryRepository).searchReviews(
                null, null, null,
                "createdAt", "DESC", null, null, 10
        );
    }

    // 리뷰 좋아요

    @Test
    void toggleLike_정상적으로_좋아요를_등록한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        Review review = new Review(book, user, "좋은 리뷰입니다.", 5);

        when(reviewRepository.findByIdAndDeletedAtIsNullForUpdate(reviewId))
                .thenReturn(Optional.of(review));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(reviewLikeRepository.findByReview_IdAndUser_Id(reviewId, userId))
                .thenReturn(Optional.empty());

        var result = reviewService.toggleLike(reviewId, userId);

        assertNotNull(result);
        assertEquals(1, review.getLikeCount());

        verify(reviewLikeRepository).save(any(ReviewLike.class));
        verify(reviewLikeRepository, never()).delete(any(ReviewLike.class));
    }

    @Test
    void toggleLike_이미_좋아요한_경우_취소한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Book book = createBook();
        User user = createUser();

        Review review = new Review(book, user, "좋은 리뷰입니다.", 5);
        review.increaseLikeCount();

        ReviewLike existingLike = new ReviewLike(review, user);

        when(reviewRepository.findByIdAndDeletedAtIsNullForUpdate(reviewId))
                .thenReturn(Optional.of(review));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(reviewLikeRepository.findByReview_IdAndUser_Id(reviewId, userId))
                .thenReturn(Optional.of(existingLike));

        var result = reviewService.toggleLike(reviewId, userId);

        assertNotNull(result);
        assertEquals(0, review.getLikeCount());

        verify(reviewLikeRepository).delete(existingLike);
        verify(reviewLikeRepository, never()).save(any(ReviewLike.class));
    }

    @Test
    void toggleLike_존재하지_않는_리뷰면_예외가_발생한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(reviewRepository.findByIdAndDeletedAtIsNullForUpdate(reviewId))
                .thenReturn(Optional.empty());

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.toggleLike(reviewId, userId)
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(reviewLikeRepository);
    }

    @Test
    void toggleLike_존재하지_않는_사용자면_예외가_발생한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Review review = new Review(
                createBook(),
                createUser(),
                "좋은 리뷰입니다.",
                5
        );

        when(reviewRepository.findByIdAndDeletedAtIsNullForUpdate(reviewId))
                .thenReturn(Optional.of(review));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.toggleLike(reviewId, userId)
        );

        verify(reviewLikeRepository, never())
                .findByReview_IdAndUser_Id(any(), any());

        verify(reviewLikeRepository, never()).save(any(ReviewLike.class));
        assertEquals(0, review.getLikeCount());
    }
}
