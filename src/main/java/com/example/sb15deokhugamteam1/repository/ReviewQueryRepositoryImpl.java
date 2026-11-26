
package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Review> searchReviews(
            UUID userId,
            UUID bookId,
            String keyword,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit
    ) {
        // 1. 기본 JPQL 생성
        StringBuilder jpql = new StringBuilder("""
                SELECT r
                FROM Review r
                WHERE r.deletedAt IS NULL
                """);

        // 2. 작성자 ID 검색 조건
        if (userId != null) {
            jpql.append(" AND r.user.id = :userId");
        }

        // 3. 도서 ID 검색 조건
        if (bookId != null) {
            jpql.append(" AND r.book.id = :bookId");
        }

        // 4. 닉네임 또는 리뷰 내용 검색 조건
        if (keyword != null && !keyword.isBlank()) {
            jpql.append("""
                    AND (
                        LOWER(r.user.nickname) LIKE LOWER(:keyword)
                        OR LOWER(r.content) LIKE LOWER(:keyword)
                    )
                    """);
        }

        // 5. 정렬 기준 설정
        String actualOrderBy = orderBy == null ? "createdAt" : orderBy;

        String sortField = switch (actualOrderBy) {
            case "createdAt" -> "r.createdAt";
            case "rating" -> "r.rating";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 정렬 기준입니다."
            );
        };

        // 6. 정렬 방향 설정
        String sortDirection = switch (
                direction == null ? "DESC" : direction
                ) {
            case "ASC" -> "ASC";
            case "DESC" -> "DESC";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 정렬 방향입니다."
            );
        };

        boolean isAsc = "ASC".equals(sortDirection);

        // 7. 커서 값 보관
        Integer cursorRating = null;
        Instant cursorCreatedAt = null;
        UUID cursorId = null;

        if (cursor != null && !cursor.isBlank()) {
            String[] cursorParts = cursor.split("\\|");

            try {
                if ("rating".equals(actualOrderBy)) {
                    if (cursorParts.length != 3) {
                        throw new IllegalArgumentException();
                    }

                    cursorRating = Integer.parseInt(cursorParts[0]);
                    cursorCreatedAt = Instant.parse(cursorParts[1]);
                    cursorId = UUID.fromString(cursorParts[2]);

                } else {
                    if (cursorParts.length != 2) {
                        throw new IllegalArgumentException();
                    }

                    cursorCreatedAt = Instant.parse(cursorParts[0]);
                    cursorId = UUID.fromString(cursorParts[1]);
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(
                        "잘못된 페이지 커서입니다."
                );
            }

            // 8. 다음 페이지 조회 조건
            if ("rating".equals(actualOrderBy)) {
                if (isAsc) {
                    jpql.append("""
                            AND (
                                r.rating > :cursorRating
                                OR (
                                    r.rating = :cursorRating
                                    AND r.createdAt > :cursorCreatedAt
                                )
                                OR (
                                    r.rating = :cursorRating
                                    AND r.createdAt = :cursorCreatedAt
                                    AND r.id > :cursorId
                                )
                            )
                            """);
                } else {
                    jpql.append("""
                            AND (
                                r.rating < :cursorRating
                                OR (
                                    r.rating = :cursorRating
                                    AND r.createdAt < :cursorCreatedAt
                                )
                                OR (
                                    r.rating = :cursorRating
                                    AND r.createdAt = :cursorCreatedAt
                                    AND r.id < :cursorId
                                )
                            )
                            """);
                }

            } else {
                if (isAsc) {
                    jpql.append("""
                            AND (
                                r.createdAt > :cursorCreatedAt
                                OR (
                                    r.createdAt = :cursorCreatedAt
                                    AND r.id > :cursorId
                                )
                            )
                            """);
                } else {
                    jpql.append("""
                            AND (
                                r.createdAt < :cursorCreatedAt
                                OR (
                                    r.createdAt = :cursorCreatedAt
                                    AND r.id < :cursorId
                                )
                            )
                            """);
                }
            }
        }

        // 9. 정렬 조건 추가
        jpql.append(" ORDER BY ")
                .append(sortField)
                .append(" ")
                .append(sortDirection);

        if ("rating".equals(actualOrderBy)) {
            jpql.append(", r.createdAt ")
                    .append(sortDirection);
        }

        jpql.append(", r.id ")
                .append(sortDirection);

        // 10. JPQL을 쿼리 객체로 변환
        var query = entityManager.createQuery(
                jpql.toString(),
                Review.class
        );

        // 11. 검색 파라미터 전달
        if (userId != null) {
            query.setParameter("userId", userId);
        }

        if (bookId != null) {
            query.setParameter("bookId", bookId);
        }

        if (keyword != null && !keyword.isBlank()) {
            query.setParameter(
                    "keyword",
                    "%" + keyword.trim() + "%"
            );
        }

        // 12. 커서 파라미터 전달
        if (cursorId != null) {
            query.setParameter("cursorId", cursorId);
            query.setParameter("cursorCreatedAt", cursorCreatedAt);

            if ("rating".equals(actualOrderBy)) {
                query.setParameter("cursorRating", cursorRating);
            }
        }

        // 13. 조회 결과 반환
        return query
                .setMaxResults(limit + 1)
                .getResultList();
    }

    @Override
    public long countReviews(
            UUID userId,
            UUID bookId,
            String keyword
    ) {
        // 1. 전체 리뷰 개수를 세는 JPQL 생성
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(r)
                FROM Review r
                WHERE r.deletedAt IS NULL
                """);

        // 2. 작성자 ID 검색 조건
        if (userId != null) {
            jpql.append(" AND r.user.id = :userId");
        }

        // 3. 도서 ID 검색 조건
        if (bookId != null) {
            jpql.append(" AND r.book.id = :bookId");
        }

        // 4. 닉네임 또는 리뷰 내용 검색 조건
        if (keyword != null && !keyword.isBlank()) {
            jpql.append("""
                    AND (
                        LOWER(r.user.nickname) LIKE LOWER(:keyword)
                        OR LOWER(r.content) LIKE LOWER(:keyword)
                    )
                    """);
        }

        // 5. JPQL을 쿼리 객체로 변환
        var query = entityManager.createQuery(
                jpql.toString(),
                Long.class
        );

        // 6. 검색 파라미터 전달
        if (userId != null) {
            query.setParameter("userId", userId);
        }

        if (bookId != null) {
            query.setParameter("bookId", bookId);
        }

        if (keyword != null && !keyword.isBlank()) {
            query.setParameter(
                    "keyword",
                    "%" + keyword.trim() + "%"
            );
        }

        // 7. 검색 조건에 해당하는 리뷰 개수 반환
        return query.getSingleResult();
    }
}