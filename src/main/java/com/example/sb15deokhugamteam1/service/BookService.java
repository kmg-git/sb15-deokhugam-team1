package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.request.BookCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.BookUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.BookDto;
import com.example.sb15deokhugamteam1.entity.Book;
import com.example.sb15deokhugamteam1.exception.book.BookNotFoundException;
import com.example.sb15deokhugamteam1.exception.book.DuplicateIsbnException;
import com.example.sb15deokhugamteam1.exception.book.InvalidIsbnException;
import com.example.sb15deokhugamteam1.mapper.dto.BookMapper;
import com.example.sb15deokhugamteam1.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    /** schema.sql 의 ck_books_isbn 과 같은 규칙: 978/979로 시작하는 13자리 숫자 */
    private static final Pattern ISBN_13 = Pattern.compile("^97[89]\\d{10}$");

    private static final String ACTIVE_ISBN_INDEX = "uq_books_active_isbn";

    private final BookRepository bookRepository;

    @Transactional
    public BookDto create(BookCreateRequest request) {
        String isbn = normalizeIsbn(request.isbn());
        if (isbn != null && bookRepository.existsByIsbnAndDeletedAtIsNull(isbn)) {
            throw new DuplicateIsbnException(isbn);
        }

        // TODO S3 연동 후 표지 이미지 URL 저장
        Book book = new Book(
                request.title(),
                request.author(),
                request.description(),
                request.publisher(),
                request.publishedDate(),
                isbn,
                null
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

    public BookDto find(UUID bookId) {
        return BookMapper.toDto(getActiveBook(bookId));
    }

    @Transactional
    public BookDto update(UUID bookId, BookUpdateRequest request) {
        Book book = getActiveBook(bookId);
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

    /** 물리 삭제: 논리 삭제된 도서도 지울 수 있다 */
    @Transactional
    public void hardDelete(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        bookRepository.delete(book);
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

    /** 빈 값은 null, 하이픈·공백은 제거한 뒤 ISBN-13 형식인지 검사 */
    private static String normalizeIsbn(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String isbn = raw.replaceAll("[\\s-]", "");
        if (!ISBN_13.matcher(isbn).matches()) {
            throw new InvalidIsbnException(raw);
        }
        return isbn;
    }
}
