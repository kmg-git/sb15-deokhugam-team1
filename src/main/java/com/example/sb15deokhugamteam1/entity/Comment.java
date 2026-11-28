package com.example.sb15deokhugamteam1.entity;


import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;

import java.time.Instant;

@Table(name = "comments")
@Entity
@Getter
public class Comment extends BaseUpdatableEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id",nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column
    private Instant deletedAt;

    protected Comment(){}

    public Comment(Review review, User user, String content) {
        this.review = review;
        this.user = user;
        this.content = content;
    }

    public boolean isDeleted(){
        return deletedAt==null ? false : true;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }



}
