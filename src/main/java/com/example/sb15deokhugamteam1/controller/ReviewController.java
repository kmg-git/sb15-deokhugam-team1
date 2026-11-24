package com.example.sb15deokhugamteam1.controller;


import com.example.sb15deokhugamteam1.dto.request.ReviewSearchRequest;
import com.example.sb15deokhugamteam1.dto.response.ReviewPageResponse;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;


    @GetMapping
    public ReviewPageResponse search(
            @ModelAttribute ReviewSearchRequest request
            ) {
        return reviewService.search(request);
    }

}
