package com.example.sb15deokhugamteam1.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ReviewSearchRequest {

    private UUID userId;

    private UUID bookId;

    private String keyword;

    private String orderBy;

    private String direction;

    private String cursor;

    private Instant after;

    private Integer limit;

}
