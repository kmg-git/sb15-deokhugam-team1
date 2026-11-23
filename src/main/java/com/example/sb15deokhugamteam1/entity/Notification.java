package com.example.sb15deokhugamteam1.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Boolean confirmed = false;

    private Notification(User user, Review review, String message) {
        this.user = user;
        this.review = review;
        this.message = message;
    }

    public static Notification create(User user, Review review, String message) {
        return new Notification(user,review,message);
    }

    public void updateConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
