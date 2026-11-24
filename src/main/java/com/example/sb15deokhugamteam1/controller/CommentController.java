package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
}
