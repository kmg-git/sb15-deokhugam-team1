
package com.example.sb15deokhugamteam1.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sb15deokhugamteam1.entity.Book;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = "spring.test.database.replace=NONE")
@Import(ReviewQueryRepositoryImpl.class)
class ReviewQueryRepositoryImplTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ReviewQueryRepositoryImpl reviewQueryRepository;

    private UUID userId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        Book book = new Book(
                "테스트 도서",
                "테스트 작가",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2026, 1, 1),
                "9781234567890",
                null
        );

        User user = new User(
                "test@example.com",
                "테스트유저",
                "password"
        );

        // JPA Auditing에 의존하지 않고 생성일을 직접 설정
        setAuditFields(book, now);
        setAuditFields(user, now);

        entityManager.persist(book);
        entityManager.persist(user);
        entityManager.flush();

        userId = user.getId();
        bookId = book.getId();

        saveReview(
                book, user, "첫 번째 리뷰", 3,
                Instant.parse("2026-11-25T10:00:00Z")
        );

        saveReview(
                book, user, "두 번째 리뷰", 4,
                Instant.parse("2026-11-26T10:00:00Z")
        );

        saveReview(
                book, user, "세 번째 리뷰", 5,
                Instant.parse("2026-11-27T10:00:00Z")
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("최신순 검색과 커서 페이지 조회가 정상 동작한다")
    void searchReviews_createdAtCursor_returnsNextPage() {
        List<Review> firstPage = reviewQueryRepository.searchReviews(
                null,
                bookId,
                null,
                "createdAt",
                "DESC",
                null,
                null,
                1
        );

        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getContent())
                .isEqualTo("세 번째 리뷰");

        Review lastReview = firstPage.get(0);

        String cursor = lastReview.getCreatedAt()
                + "|" + lastReview.getId();

        List<Review> secondPage = reviewQueryRepository.searchReviews(
                null,
                bookId,
                null,
                "createdAt",
                "DESC",
                cursor,
                lastReview.getCreatedAt(),
                1
        );

        assertThat(secondPage)
                .extracting(Review::getContent)
                .containsExactly("두 번째 리뷰", "첫 번째 리뷰");
    }

    @Test
    @DisplayName("평점순 검색과 커서 페이지 조회가 정상 동작한다")
    void searchReviews_ratingCursor_returnsNextPage() {
        List<Review> firstPage = reviewQueryRepository.searchReviews(
                null,
                bookId,
                null,
                "rating",
                "DESC",
                null,
                null,
                1
        );

        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getRating()).isEqualTo(5);

        Review lastReview = firstPage.get(0);

        String cursor = lastReview.getRating()
                + "|" + lastReview.getCreatedAt()
                + "|" + lastReview.getId();

        List<Review> secondPage = reviewQueryRepository.searchReviews(
                null,
                bookId,
                null,
                "rating",
                "DESC",
                cursor,
                lastReview.getCreatedAt(),
                1
        );

        assertThat(secondPage)
                .extracting(Review::getRating)
                .containsExactly(4, 3);
    }

    @Test
    @DisplayName("검색 조건에 맞는 리뷰 개수를 조회한다")
    void countReviews_returnsMatchingCount() {
        long count = reviewQueryRepository.countReviews(
                null,
                bookId,
                "두 번째"
        );

        assertThat(count).isEqualTo(1);
    }

    private void saveReview(
            Book book,
            User user,
            String content,
            Integer rating,
            Instant createdAt
    ) {
        Review review = new Review(book, user, content, rating);

        setAuditFields(review, createdAt);

        entityManager.persist(review);
    }

    private void setAuditFields(Object entity, Instant createdAt) {
        ReflectionTestUtils.setField(
                entity, "createdAt", createdAt
        );

        ReflectionTestUtils.setField(
                entity, "updatedAt", createdAt
        );
    }
}
