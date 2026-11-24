package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.request.BookCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.BookUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.BookDto;
import com.example.sb15deokhugamteam1.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // TODO thumbnailImage 는 S3 연동 후 처리 (지금은 받기만 하고 저장하지 않음)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> create(
            @RequestPart("bookData") @Valid BookCreateRequest request,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> find(@PathVariable UUID bookId) {
        return ResponseEntity.ok(bookService.find(bookId));
    }

    @PatchMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> update(
            @PathVariable UUID bookId,
            @RequestPart("bookData") @Valid BookUpdateRequest request,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        return ResponseEntity.ok(bookService.update(bookId, request));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID bookId) {
        bookService.softDelete(bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bookId}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID bookId) {
        bookService.hardDelete(bookId);
        return ResponseEntity.noContent().build();
    }
}
