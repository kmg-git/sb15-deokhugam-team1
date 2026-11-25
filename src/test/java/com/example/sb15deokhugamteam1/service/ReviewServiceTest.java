package com.example.sb15deokhugamteam1.service;


import com.example.sb15deokhugamteam1.dto.request.ReviewCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.ReviewUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.ReviewDto;
import com.example.sb15deokhugamteam1.entity.Book;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.repository.BookRepository;
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
import com.example.sb15deokhugamteam1.dto.request.ReviewSearchRequest;
import com.example.sb15deokhugamteam1.dto.response.ReviewPageResponse;

import java.time.Instant;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewQueryRepository reviewQueryRepository;

    @InjectMocks
    private  ReviewService reviewService;

    @Test
    void create_도서가_존재하지_않으면_예외가_발생한다() {

        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(request.getBookId()).thenReturn(bookId);

        when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                EntityNotFoundException.class,() -> reviewService.create(userId, request)
        );

        verify(reviewRepository, never()).saveAndFlush(any());
        verifyNoInteractions(userRepository);
    }

    @Test
    void create_사용자가_존재하지_않으면_예외가_발생한다() {

        // given: 도서는 존재하지만 사용자는 존재하지 않는 상황
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(request.getBookId()).thenReturn(bookId);

        when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .thenReturn(java.util.Optional.of(book));

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.empty());

        // when & then: 사용자 조회에서 예외 발생
        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.create(userId, request)
        );

        // 리뷰 저장은 실행되지 않아야 함
        verify(reviewRepository, never()).saveAndFlush(any());

    }
    @Test
    void create_삭제된_사용자는_리뷰를_등록할_수_없다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        user.softDelete();

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(request.getBookId()).thenReturn(bookId);

        when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .thenReturn(java.util.Optional.of(book));

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.of(user));

        // when & then
        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.create(userId, request)
        );

        verify(reviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_정상적으로_리뷰를_등록한다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(request.getBookId()).thenReturn(bookId);
        when(request.getContent()).thenReturn("정말 재미있는 책입니다.");
        when(request.getRating()).thenReturn(5);

        when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .thenReturn(java.util.Optional.of(book));

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.of(user));

        when(reviewRepository.saveAndFlush(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookRepository.increaseReview(book.getId(), 5))
                .thenReturn(1);

        // when
        ReviewDto result = reviewService.create(userId, request);

        // then
        assertNotNull(result);
        assertEquals("정말 재미있는 책입니다.", result.getContent());
        assertEquals(5, result.getRating());

        verify(reviewRepository).saveAndFlush(any(Review.class));
        verify(bookRepository).increaseReview(book.getId(), 5);
    }

    @Test
    void create_도서_통계_갱신에_실패하면_예외가_발생한다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        ReviewCreateRequest request = mock(ReviewCreateRequest.class);

        when(request.getBookId()).thenReturn(bookId);
        when(request.getContent()).thenReturn("테스트 리뷰");
        when(request.getRating()).thenReturn(5);

        when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .thenReturn(java.util.Optional.of(book));

        when(userRepository.findById(userId))
                .thenReturn(java.util.Optional.of(user));

        when(reviewRepository.saveAndFlush(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookRepository.increaseReview(book.getId(), 5))
                .thenReturn(0);

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> reviewService.create(userId, request)
        );

        verify(reviewRepository).saveAndFlush(any(Review.class));
        verify(bookRepository).increaseReview(book.getId(), 5);
    }

    @Test
    void update_존재하지_않는_리뷰면_예외가_발생한다() {

        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.update(reviewId, userId, request)
        );

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
        verifyNoInteractions(bookRepository);
    }
    @Test
    void update_삭제된_사용자는_리뷰를_수정할_수_없다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );
        user.softDelete();

        Review review = new Review(book, user, "기존 리뷰", 4);

        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> reviewService.update(reviewId, userId, request)
        );

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
        verify(bookRepository, never()).changeReviewRating(any(), anyInt());
    }

    @Test
    void update_작성자가_아닌_사용자는_리뷰를_수정할_수_없다() {

        // given
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User owner = new User(
                "owner@test.com",
                "작성자",
                "password"
        );

        ReflectionTestUtils.setField(owner, "id", ownerId);


        Review review = new Review(book, owner, "기존 리뷰", 4);

        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> reviewService.update(reviewId, otherUserId, request)
        );

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
        verify(bookRepository, never()).changeReviewRating(any(), anyInt());
    }

    @Test
    void update_정상적으로_리뷰를_수정한다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        // JPA가 생성하는 ID를 테스트에서 직접 설정
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(
                book,
                user,
                "기존 리뷰",
                3
        );

        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(request.getContent()).thenReturn("수정된 리뷰");
        when(request.getRating()).thenReturn(5);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(bookRepository.changeReviewRating(book.getId(), 2))
                .thenReturn(1);

        // when
        ReviewDto result = reviewService.update(reviewId, userId, request);

        // then
        assertNotNull(result);
        assertEquals("수정된 리뷰", result.getContent());
        assertEquals(5, result.getRating());

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
        verify(bookRepository).changeReviewRating(book.getId(), 2);
    }

    @Test
    void update_별점이_변경되지_않으면_도서_통계를_갱신하지_않는다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);

        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(request.getContent()).thenReturn("수정된 리뷰");
        when(request.getRating()).thenReturn(3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when
        ReviewDto result = reviewService.update(reviewId, userId, request);

        // then
        assertNotNull(result);
        assertEquals("수정된 리뷰", result.getContent());
        assertEquals(3, result.getRating());

        verify(bookRepository, never())
                .changeReviewRating(any(), anyInt());
    }
    @Test
    void update_별점이_null이면_기존_별점을_유지한다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);

        ReviewUpdateRequest request = mock(ReviewUpdateRequest.class);

        when(request.getContent()).thenReturn("내용만 수정");
        when(request.getRating()).thenReturn(null);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when
        ReviewDto result = reviewService.update(reviewId, userId, request);

        // then
        assertNotNull(result);
        assertEquals("내용만 수정", result.getContent());
        assertEquals(3, result.getRating());

        verify(bookRepository, never())
                .changeReviewRating(any(), anyInt());
    }
    @Test
    void update_도서_통계_갱신에_실패하면_예외가_발생한다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

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

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> reviewService.update(reviewId, userId, request)
        );

        verify(bookRepository).changeReviewRating(book.getId(), 2);
    }

    @Test
    void delete_존재하지_않는_리뷰면_예외가_발생한다() {

        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.delete(reviewId, userId)
        );

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
        verify(bookRepository, never()).decreaseReview(any(), anyInt());
    }

    @Test
    void delete_삭제된_사용자는_리뷰를_삭제할_수_없다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        user.softDelete();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> reviewService.delete(reviewId, userId)
        );

        verify(bookRepository, never())
                .decreaseReview(any(), anyInt());
    }

    @Test
    void delete_작성자가_아닌_사용자는_리뷰를_삭제할_수_없다() {

        // given
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User owner = new User(
                "owner@test.com",
                "작성자",
                "password"
        );

        ReflectionTestUtils.setField(owner, "id", ownerId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, owner, "기존 리뷰", 3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> reviewService.delete(reviewId, otherUserId)
        );

        verify(bookRepository, never())
                .decreaseReview(any(), anyInt());
    }
    @Test
    void delete_정상적으로_리뷰를_삭제한다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(bookRepository.decreaseReview(book.getId(), 3))
                .thenReturn(1);

        // when
        reviewService.delete(reviewId, userId);

        // then
        assertTrue(review.isDeleted());

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
        verify(bookRepository).decreaseReview(book.getId(), 3);
    }
    @Test
    void delete_도서_통계_갱신에_실패하면_예외가_발생한다() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "기존 리뷰", 3);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        when(bookRepository.decreaseReview(book.getId(), 3))
                .thenReturn(0);

        // when & then
        assertThrows(
                IllegalStateException.class,
                () -> reviewService.delete(reviewId, userId)
        );

        verify(bookRepository).decreaseReview(book.getId(), 3);
    }

    @Test
    void get_존재하지_않는_리뷰면_예외가_발생한다() {

        // given
        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.get(reviewId)
        );

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
    }

    @Test
    void get_정상적으로_리뷰를_조회한다() {

        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        Review review = new Review(book, user, "조회할 리뷰", 4);

        when(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .thenReturn(Optional.of(review));

        // when
        ReviewDto result = reviewService.get(reviewId);

        // then
        assertNotNull(result);
        assertEquals("조회할 리뷰", result.getContent());
        assertEquals(4, result.getRating());

        verify(reviewRepository).findByIdAndDeletedAtIsNull(reviewId);
    }

    @Test
    void getAllByBook_정상적으로_리뷰_목록을_조회한다() {

        // given
        UUID bookId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@test.com",
                "테스트 사용자",
                "password"
        );

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

        // when
        Page<ReviewDto> result = reviewService.getAllByBook(bookId, pageable);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("첫 번째 리뷰", result.getContent().get(0).getContent());
        assertEquals("두 번째 리뷰", result.getContent().get(1).getContent());

        verify(reviewRepository)
                .findAllByBook_IdAndDeletedAtIsNull(bookId, pageable);
    }
    @Test
    void search_정상적으로_리뷰를_검색한다() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        Book book = new Book(
                "테스트 도서",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                java.time.LocalDate.of(2025, 1, 1),
                "9781234567890",
                null
        );

        User user  = new User(
                "test@test.com",
                "테스트 사용자",
                "paasword"
        );

        ReflectionTestUtils.setField(book, "id", bookId);
        ReflectionTestUtils.setField(user, "id", userId);

        Review review = new  Review(
                book,
                user,
                "정상 검색 테스트 리뷰",
                5
        );

        ReflectionTestUtils.setField(review,"id", UUID.randomUUID());
        ReflectionTestUtils.setField(
                review,
                "createdAt",
                Instant.parse("2026-11-25T10:00:00Z")
        );

    }











}
