package com.example.sb15deokhugamteam1.event;

import com.example.sb15deokhugamteam1.entity.Period;

import java.util.UUID;

public record PopularReviewRankedEvent(
        UUID reviewId,
        UUID reviewAuthorId,
        Period period,
        long rank
) {
}
