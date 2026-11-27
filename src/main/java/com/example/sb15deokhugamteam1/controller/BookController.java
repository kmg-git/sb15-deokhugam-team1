package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponseBookDto;
import com.example.sb15deokhugamteam1.dto.request.BookCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.BookSearchCondition;
import com.example.sb15deokhugamteam1.dto.request.BookSortField;
import com.example.sb15deokhugamteam1.dto.request.BookUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.BookDto;
import com.example.sb15deokhugamteam1.dto.response.BookInfoDto;
import com.example.sb15deokhugamteam1.service.BookInfoService;
import com.example.sb15deokhugamteam1.service.BookService;
import com.example.sb15deokhugamteam1.service.IsbnOcrService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookInfoService bookInfoService;
    private final IsbnOcrService isbnOcrService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> create(
            @RequestPart("bookData") @Valid BookCreateRequest request,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request, thumbnailImage));
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseBookDto> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Instant after,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    ) {
        BookSearchCondition condition = new BookSearchCondition(
                keyword, BookSortField.from(orderBy), direction, cursor, after, limit);
        return ResponseEntity.ok(bookService.search(condition));
    }

    /** ISBN 으로 카카오에서 도서 정보를 가져온다 (등록 화면 자동 채우기용) */
    @GetMapping("/info")
    public ResponseEntity<BookInfoDto> getInfo(@RequestParam String isbn) {
        return ResponseEntity.ok(bookInfoService.getByIsbn(isbn));
    }

    /** 도서 사진(바코드)에서 ISBN 을 읽어 문자열로 돌려준다 */
    @PostMapping(value = "/isbn/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> recognizeIsbn(@RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(isbnOcrService.recognize(image));
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
        return ResponseEntity.ok(bookService.update(bookId, request, thumbnailImage));
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
