
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
        StringBuilder jpql = new StringBuilder("""
                SELECT r
                FROM Review r
                WHERE r.deletedAt IS NULL
                """);

        if (userId != null) {
            jpql.append(" AND r.user.id = :userId ");
        }

        if (bookId != null) {
            jpql.append(" AND r.book.id = :bookId ");
        }

        if (keyword != null && !keyword.isBlank()) {
            jpql.append("""
                     AND (
                         LOWER(r.user.nickname) LIKE LOWER(:keyword)
                         OR LOWER(r.content) LIKE LOWER(:keyword)
                         OR LOWER(r.book.title) LIKE LOWER(:keyword)
                     )
                    """);
        }

        String actualOrderBy = orderBy == null || orderBy.isBlank()
                ? "createdAt"
                : orderBy;

        String sortField = switch (actualOrderBy) {
            case "createdAt" -> "r.createdAt";
            case "rating" -> "r.rating";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 정렬 기준입니다."
            );
        };

        String actualDirection = direction == null || direction.isBlank()
                ? "DESC"
                : direction.toUpperCase(java.util.Locale.ROOT);

        String sortDirection = switch (actualDirection) {
            case "ASC" -> "ASC";
            case "DESC" -> "DESC";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 정렬 방향입니다."
            );
        };

        boolean isAsc = "ASC".equals(sortDirection);

        Integer cursorRating = null;
        Instant cursorCreatedAt = null;
        UUID cursorId = null;

        if ((cursor == null || cursor.isBlank()) && after != null) {
            throw new IllegalArgumentException("보조 커서는 페이지 커서와 함께 전달해야 합니다.");
        }

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

            // 커서 파싱 성공 후 보조 커서 검증
            if (after != null && !after.equals(cursorCreatedAt)) {
                throw new IllegalArgumentException(
                        "커서와 보조 커서가 일치하지 않습니다."
                );
            }

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

        var query = entityManager.createQuery(
                jpql.toString(),
                Review.class
        );

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

        if (cursorId != null) {
            query.setParameter("cursorId", cursorId);
            query.setParameter("cursorCreatedAt", cursorCreatedAt);

            if ("rating".equals(actualOrderBy)) {
                query.setParameter("cursorRating", cursorRating);
            }
        }

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
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(r)
                FROM Review r
                WHERE r.deletedAt IS NULL
                """);

        if (userId != null) {
            jpql.append(" AND r.user.id = :userId ");
        }

        if (bookId != null) {
            jpql.append(" AND r.book.id = :bookId ");
        }

        if (keyword != null && !keyword.isBlank()) {
            jpql.append("""
                     AND (
                         LOWER(r.user.nickname) LIKE LOWER(:keyword)
                         OR LOWER(r.content) LIKE LOWER(:keyword)
                         OR LOWER(r.book.title) LIKE LOWER(:keyword)
                     )
                    """);
        }

        var query = entityManager.createQuery(
                jpql.toString(),
                Long.class
        );

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

        return query.getSingleResult();
    }
}
