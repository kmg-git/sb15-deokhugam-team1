package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Review;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReviewQueryRepository  {

    List<Review> searchReviews(
            UUID userId,
            UUID bookId,
            String keyword,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit
    );

    long countReviews(
            UUID userId,
            UUID bookId,
            String keyword
    );


}
