package com.example.sb15deokhugamteam1.entity;


import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;

import java.time.LocalDateTime;

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

}
