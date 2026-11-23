package com.example.sb15deokhugamteam1.entity;


import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Table(name = "comments")
@Entity
@Getter
public class Comment extends BaseUpdatableEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviews",nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users",nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column
    private LocalDateTime DeletedAt;

    protected Comment(){}

}
