
package com.example.sb15deokhugamteam1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.Instant;
@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseUpdatableEntity {

    // 어떤 도서에 작성된 리뷰인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // 어떤 사용자가 작성한 리뷰인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 리뷰 내용
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    // 리뷰 평점
    @Column(nullable = false)
    private Integer rating;

    // 좋아요 개수
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    // 댓글 개수
    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    // 논리 삭제 시각
    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Review(Book book, User user, String content, Integer rating) {
        this.book = requireNonNull(book, "book");
        this.user = requireNonNull(user, "user");
        this.content = requireText(content, "content");
        this.rating = requireValidRating(rating);
    }

    // 전달된 값만 수정
    public void update(String content, Integer rating) {
        if (content != null) {
            this.content = requireText(content, "content");
        }

        if (rating != null) {
            this.rating = requireValidRating(rating);
        }
    }

    // 논리 삭제: 최초 삭제 시각 유지
    public void softDelete() {
        if (this.deletedAt == null) {
            this.deletedAt = Instant.now();
        }
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
        return value;
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(
                    field + " must not be null"
            );
        }
        return value;
    }

    private static Integer requireValidRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "rating must be between 1 and 5"
            );
        }
        return rating;
    }
}
