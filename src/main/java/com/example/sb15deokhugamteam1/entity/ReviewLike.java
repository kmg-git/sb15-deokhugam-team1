package com.example.sb15deokhugamteam1.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "review_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_likes_review_user",
                        columnNames = {"review_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ReviewLike(Review review, User user) {
        if (review == null) {
            throw new IllegalArgumentException("review must not be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        this.review = review;
        this.user = user;
    }

}
