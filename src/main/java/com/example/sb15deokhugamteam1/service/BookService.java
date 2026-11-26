package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponseBookDto;
import com.example.sb15deokhugamteam1.dto.request.BookCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.BookSearchCondition;
import com.example.sb15deokhugamteam1.dto.request.BookSortField;
import com.example.sb15deokhugamteam1.dto.request.BookUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.BookDto;
import com.example.sb15deokhugamteam1.entity.Book;
import com.example.sb15deokhugamteam1.exception.book.BookNotFoundException;
import com.example.sb15deokhugamteam1.exception.book.DuplicateIsbnException;
import com.example.sb15deokhugamteam1.exception.book.InvalidCursorException;
import com.example.sb15deokhugamteam1.mapper.dto.BookMapper;
import com.example.sb15deokhugamteam1.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private static final String ACTIVE_ISBN_INDEX = "uq_books_active_isbn";

    private final BookRepository bookRepository;
    private final BookThumbnailService bookThumbnailService;

    @Transactional
    public BookDto create(BookCreateRequest request, MultipartFile thumbnailImage) {
        String isbn = IsbnNormalizer.normalize(request.isbn());
        if (isbn != null && bookRepository.existsByIsbnAndDeletedAtIsNull(isbn)) {
            throw new DuplicateIsbnException(isbn);
        }

        // 중복 검사를 통과한 뒤에 올려야 실패할 요청 때문에 파일이 쌓이지 않는다
        String thumbnailUrl = bookThumbnailService.store(thumbnailImage);
        Book book = new Book(
                request.title(),
                request.author(),
                request.description(),
                request.publisher(),
                request.publishedDate(),
                isbn,
                thumbnailUrl
        );

        try {
            // 바로 INSERT 해서, 동시에 같은 ISBN이 들어온 경우 DB 유니크 인덱스 위반을 여기서 잡는다
            return BookMapper.toDto(bookRepository.saveAndFlush(book));
        } catch (DataIntegrityViolationException e) {
            // ISBN 유니크 인덱스 위반일 때만 409로 바꾸고, 다른 DB 오류는 그대로 던진다
            if (isbn != null && isActiveIsbnViolation(e)) {
                throw new DuplicateIsbnException(isbn);
            }
            throw e;
        }
    }

    /** 도서 목록: 검색어로 거르고, 정렬 기준 + createdAt 순서로 커서 다음 limit 개 */
    public CursorPageResponseBookDto search(BookSearchCondition condition) {
        Object cursorValue = parseCursor(condition);

        List<Book> rows = bookRepository.search(condition, cursorValue);
        boolean hasNext = rows.size() > condition.limit();
        List<Book> page = hasNext ? rows.subList(0, condition.limit()) : rows;

        Book last = page.isEmpty() ? null : page.get(page.size() - 1);
        String nextCursor = hasNext ? cursorOf(last, condition.sortField()) : null;
        Instant nextAfter = hasNext ? last.getCreatedAt() : null;

        return new CursorPageResponseBookDto(
                page.stream().map(BookMapper::toDto).toList(),
                nextCursor,
                nextAfter,
                page.size(),
                bookRepository.countByKeyword(condition.keyword()),
                hasNext
        );
    }

    public BookDto find(UUID bookId) {
        return BookMapper.toDto(getActiveBook(bookId));
    }

    @Transactional
    public BookDto update(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage) {
        Book book = getActiveBook(bookId);
        String newThumbnailUrl = bookThumbnailService.store(thumbnailImage);
        if (newThumbnailUrl != null) {
            bookThumbnailService.deleteAfterCommit(book.getThumbnailUrl());
            book.changeThumbnailUrl(newThumbnailUrl);
        }
        book.update(
                request.title(),
                request.author(),
                request.description(),
                request.publisher(),
                request.publishedDate()
        );
        // flush 시점에 updatedAt(@LastModifiedDate)이 채워지므로 응답 전에 반영
        bookRepository.flush();
        return BookMapper.toDto(book);
    }

    /** 논리 삭제: deleted_at 만 기록한다. 이미 삭제된 도서는 404 */
    @Transactional
    public void softDelete(UUID bookId) {
        getActiveBook(bookId).softDelete(Instant.now());
    }

    /** 물리 삭제: 논리 삭제된 도서도 지울 수 있다. 표지 파일도 함께 지운다 (논리 삭제는 복구할 수 있어서 남겨 둔다) */
    @Transactional
    public void hardDelete(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        bookRepository.delete(book);
        bookThumbnailService.deleteAfterCommit(book.getThumbnailUrl());
    }

    private Book getActiveBook(UUID bookId) {
        return bookRepository.findByIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    /** schema.sql 의 uq_books_active_isbn 인덱스 위반인지 확인 */
    private static boolean isActiveIsbnViolation(DataIntegrityViolationException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve) {
                String name = cve.getConstraintName();
                return name != null && name.toLowerCase(Locale.ROOT).contains(ACTIVE_ISBN_INDEX);
            }
        }
        return false;
    }

    /** cursor 문자열을 정렬 기준에 맞는 타입으로 바꾼다. cursor·after 는 둘 다 있거나 둘 다 없어야 한다 */
    private static Object parseCursor(BookSearchCondition condition) {
        String cursor = condition.cursor();
        Instant after = condition.after();
        if (cursor == null && after == null) {
            return null;
        }
        if (cursor == null || after == null) {
            throw new InvalidCursorException(cursor, after);
        }
        try {
            return switch (condition.sortField()) {
                case TITLE -> cursor;
                case PUBLISHED_DATE -> LocalDate.parse(cursor);
                case RATING -> Double.valueOf(cursor);
                case REVIEW_COUNT -> Integer.valueOf(cursor);
            };
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new InvalidCursorException(cursor, after);
        }
    }

    /** 다음 페이지 요청에 쓸 커서: 이 페이지 마지막 책의 정렬 기준 값 */
    private static String cursorOf(Book book, BookSortField sortField) {
        return switch (sortField) {
            case TITLE -> book.getTitle();
            case PUBLISHED_DATE -> book.getPublishedDate().toString();
            case RATING -> String.valueOf(book.getRating());
            case REVIEW_COUNT -> String.valueOf(book.getReviewCount());
        };
    }
}
