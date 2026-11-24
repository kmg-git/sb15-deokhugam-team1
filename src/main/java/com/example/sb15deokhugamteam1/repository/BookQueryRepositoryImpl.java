package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.dto.request.BookSearchCondition;
import com.example.sb15deokhugamteam1.entity.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 도서 목록 검색 (Criteria API).
 *
 * <p>커서 방식: "마지막으로 본 책의 (정렬 값, createdAt)" 보다 뒤에 있는 책만 가져온다.
 * OFFSET 처럼 앞 페이지를 매번 다시 세지 않아서, 뒤 페이지로 갈수록 느려지지 않는다.
 */
public class BookQueryRepositoryImpl implements BookQueryRepository {

    private static final char ESCAPE = '\\';

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Book> search(BookSearchCondition condition, Object cursorValue) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> query = cb.createQuery(Book.class);
        Root<Book> book = query.from(Book.class);

        List<Predicate> where = baseConditions(cb, book, condition.keyword());
        if (cursorValue != null) {
            where.add(afterCursor(cb, book, condition, cursorValue));
        }

        Path<Comparable<Object>> sortPath = book.get(condition.sortField().field());
        Path<Instant> createdAt = book.get("createdAt");
        boolean asc = condition.direction() == Sort.Direction.ASC;

        query.select(book)
                .where(where.toArray(Predicate[]::new))
                .orderBy(
                        asc ? cb.asc(sortPath) : cb.desc(sortPath),
                        asc ? cb.asc(createdAt) : cb.desc(createdAt)
                );

        return em.createQuery(query)
                .setMaxResults(condition.limit() + 1)
                .getResultList();
    }

    @Override
    public long countByKeyword(String keyword) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Book> book = query.from(Book.class);

        query.select(cb.count(book))
                .where(baseConditions(cb, book, keyword).toArray(Predicate[]::new));

        return em.createQuery(query).getSingleResult();
    }

    /** 삭제 안 된 책 + 검색어(제목·저자·ISBN 부분 일치, 대소문자 무시) */
    private List<Predicate> baseConditions(CriteriaBuilder cb, Root<Book> book, String keyword) {
        List<Predicate> where = new ArrayList<>();
        where.add(cb.isNull(book.get("deletedAt")));

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + escapeLike(keyword.trim().toLowerCase(Locale.ROOT)) + "%";
            // ISBN 은 하이픈 없이 저장되므로 검색어의 하이픈·공백도 지우고 비교
            String isbnPattern = "%" + escapeLike(keyword.replaceAll("[\\s-]", "")) + "%";
            where.add(cb.or(
                    cb.like(cb.lower(book.get("title")), pattern, ESCAPE),
                    cb.like(cb.lower(book.get("author")), pattern, ESCAPE),
                    cb.like(book.get("isbn"), isbnPattern, ESCAPE)
            ));
        }
        return where;
    }

    /**
     * 내림차순이면: 정렬 값 < 커서  또는  (정렬 값 = 커서 그리고 createdAt < after)
     * 오름차순이면 부등호만 반대.
     */
    private Predicate afterCursor(CriteriaBuilder cb, Root<Book> book,
                                  BookSearchCondition condition, Object cursorValue) {
        Expression<Comparable<Object>> sortPath = book.get(condition.sortField().field());
        Expression<Instant> createdAt = book.get("createdAt");
        @SuppressWarnings("unchecked")
        Comparable<Object> cursor = (Comparable<Object>) cursorValue;
        Instant after = condition.after();

        if (condition.direction() == Sort.Direction.ASC) {
            return cb.or(
                    cb.greaterThan(sortPath, cursor),
                    cb.and(cb.equal(sortPath, cursor), cb.greaterThan(createdAt, after))
            );
        }
        return cb.or(
                cb.lessThan(sortPath, cursor),
                cb.and(cb.equal(sortPath, cursor), cb.lessThan(createdAt, after))
        );
    }

    /** 검색어에 들어 있는 % _ \ 를 글자 그대로 찾도록 처리 */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
