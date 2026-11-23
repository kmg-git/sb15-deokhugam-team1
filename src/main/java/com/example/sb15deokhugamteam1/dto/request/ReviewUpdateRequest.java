package com.example.sb15deokhugamteam1.dto.request;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewUpdateRequest {

    @Size(max = 5000)
    private String content;

    @Min(1)
    @Max(5)
    private Integer rating;
}
