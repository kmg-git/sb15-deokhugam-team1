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
import com.example.sb15deokhugamteam1.exception.book.InvalidIsbnException;
import com.example.sb15deokhugamteam1.repository.BookRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository bookRepository;

    @InjectMocks
    BookService bookService;

    private static final LocalDate PUBLISHED = LocalDate.of(2013, 12, 24);

    private static BookCreateRequest createRequest(String isbn) {
        return new BookCreateRequest("클린 코드", "로버트 C. 마틴", "설명", "인사이트", PUBLISHED, isbn);
    }

    private static Book book() {
        return new Book("클린 코드", "로버트 C. 마틴", "설명", "인사이트", PUBLISHED, "9788966260959", null);
    }

    private static DataIntegrityViolationException violation(String constraintName) {
        return new DataIntegrityViolationException("unique violation",
                new ConstraintViolationException("duplicate key", new SQLException(), constraintName));
    }

    @Nested
    class Create {

        @Test
        void 하이픈을_제거한_ISBN으로_저장한다() {
            given(bookRepository.existsByIsbnAndDeletedAtIsNull("9788966260959")).willReturn(false);
            given(bookRepository.saveAndFlush(any(Book.class))).willAnswer(inv -> inv.getArgument(0));

            BookDto result = bookService.create(createRequest("978-89-6626-095-9"));

            assertThat(result.isbn()).isEqualTo("9788966260959");
            assertThat(result.title()).isEqualTo("클린 코드");
            assertThat(result.reviewCount()).isZero();
        }

        @Test
        void ISBN이_비어_있으면_null로_저장하고_중복_검사를_하지_않는다() {
            given(bookRepository.saveAndFlush(any(Book.class))).willAnswer(inv -> inv.getArgument(0));

            BookDto result = bookService.create(createRequest("  "));

            assertThat(result.isbn()).isNull();
            verify(bookRepository, never()).existsByIsbnAndDeletedAtIsNull(any());
        }

        @Test
        void ISBN_형식이_틀리면_InvalidIsbnException() {
            assertThatThrownBy(() -> bookService.create(createRequest("12345")))
                    .isInstanceOf(InvalidIsbnException.class);
            verify(bookRepository, never()).saveAndFlush(any());
        }

        @Test
        void 이미_등록된_ISBN이면_DuplicateIsbnException() {
            given(bookRepository.existsByIsbnAndDeletedAtIsNull("9788966260959")).willReturn(true);

            assertThatThrownBy(() -> bookService.create(createRequest("9788966260959")))
                    .isInstanceOf(DuplicateIsbnException.class);
            verify(bookRepository, never()).saveAndFlush(any());
        }

        @Test
        void 동시_등록으로_DB_유니크_위반이_나도_DuplicateIsbnException() {
            given(bookRepository.existsByIsbnAndDeletedAtIsNull("9788966260959")).willReturn(false);
            given(bookRepository.saveAndFlush(any(Book.class)))
                    .willThrow(violation("uq_books_active_isbn"));

            assertThatThrownBy(() -> bookService.create(createRequest("9788966260959")))
                    .isInstanceOf(DuplicateIsbnException.class);
        }

        @Test
        void ISBN이_있어도_다른_DB_오류면_중복으로_바꾸지_않는다() {
            DataIntegrityViolationException original = new DataIntegrityViolationException("value too long");
            given(bookRepository.existsByIsbnAndDeletedAtIsNull("9788966260959")).willReturn(false);
            given(bookRepository.saveAndFlush(any(Book.class))).willThrow(original);

            assertThatThrownBy(() -> bookService.create(createRequest("9788966260959")))
                    .isSameAs(original);
        }

        @Test
        void ISBN이_없는데_DB_제약_위반이면_원래_예외를_그대로_던진다() {
            DataIntegrityViolationException original = new DataIntegrityViolationException("other constraint");
            given(bookRepository.saveAndFlush(any(Book.class))).willThrow(original);

            assertThatThrownBy(() -> bookService.create(createRequest(null)))
                    .isSameAs(original);
        }
    }

    @Nested
    class FindAndUpdate {

        @Test
        void 없는_도서를_조회하면_BookNotFoundException() {
            UUID id = UUID.randomUUID();
            given(bookRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.find(id)).isInstanceOf(BookNotFoundException.class);
        }

        @Test
        void 수정하면_엔티티_값이_바뀌고_flush_한다() {
            UUID id = UUID.randomUUID();
            Book book = book();
            given(bookRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(book));

            BookDto result = bookService.update(id,
                    new BookUpdateRequest("클린 코드 2판", "로버트 C. 마틴", "새 설명", "인사이트", PUBLISHED));

            assertThat(result.title()).isEqualTo("클린 코드 2판");
            assertThat(book.getDescription()).isEqualTo("새 설명");
            verify(bookRepository).flush();
        }
    }

    @Nested
    class Search {

        private Book bookAt(String title, double rating, Instant createdAt) {
            Book book = new Book(title, "저자", "설명", "출판사", PUBLISHED, null, null);
            ReflectionTestUtils.setField(book, "createdAt", createdAt);
            ReflectionTestUtils.setField(book, "rating", rating);
            return book;
        }

        private BookSearchCondition condition(BookSortField field, String cursor, Instant after, int limit) {
            return new BookSearchCondition(null, field, Sort.Direction.DESC, cursor, after, limit);
        }

        @Test
        void limit_보다_하나_더_오면_다음_페이지가_있고_마지막_책이_커서가_된다() {
            Instant t1 = Instant.parse("2026-11-01T00:00:00Z");
            Instant t2 = Instant.parse("2026-11-02T00:00:00Z");
            Instant t3 = Instant.parse("2026-11-03T00:00:00Z");
            given(bookRepository.search(any(), any())).willReturn(
                    List.of(bookAt("다", 4.0, t3), bookAt("나", 3.0, t2), bookAt("가", 2.0, t1)));
            given(bookRepository.countByKeyword(null)).willReturn(3L);

            CursorPageResponseBookDto result = bookService.search(condition(BookSortField.TITLE, null, null, 2));

            assertThat(result.content()).extracting(BookDto::title).containsExactly("다", "나");
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo("나");
            assertThat(result.nextAfter()).isEqualTo(t2);
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.totalElements()).isEqualTo(3L);
        }

        @Test
        void 마지막_페이지면_다음_커서가_없다() {
            given(bookRepository.search(any(), any())).willReturn(
                    List.of(bookAt("가", 2.0, Instant.parse("2026-11-01T00:00:00Z"))));

            CursorPageResponseBookDto result = bookService.search(condition(BookSortField.TITLE, null, null, 2));

            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextAfter()).isNull();
        }

        @Test
        void 평점_정렬이면_커서를_숫자로_바꿔서_넘긴다() {
            Instant after = Instant.parse("2026-11-01T00:00:00Z");
            given(bookRepository.search(any(), any())).willReturn(List.of());

            bookService.search(condition(BookSortField.RATING, "4.5", after, 10));

            verify(bookRepository).search(any(), org.mockito.ArgumentMatchers.eq(4.5));
        }

        @Test
        void 출판일_정렬이면_커서를_날짜로_바꿔서_넘긴다() {
            Instant after = Instant.parse("2026-11-01T00:00:00Z");
            given(bookRepository.search(any(), any())).willReturn(List.of());

            bookService.search(condition(BookSortField.PUBLISHED_DATE, "2013-12-24", after, 10));

            verify(bookRepository).search(any(), org.mockito.ArgumentMatchers.eq(LocalDate.of(2013, 12, 24)));
        }

        @Test
        void 커서를_정렬_기준_타입으로_읽을_수_없으면_InvalidCursorException() {
            Instant after = Instant.parse("2026-11-01T00:00:00Z");

            assertThatThrownBy(() -> bookService.search(condition(BookSortField.REVIEW_COUNT, "많음", after, 10)))
                    .isInstanceOf(InvalidCursorException.class);
        }

        @Test
        void 커서와_after_중_하나만_오면_InvalidCursorException() {
            assertThatThrownBy(() -> bookService.search(condition(BookSortField.TITLE, "가", null, 10)))
                    .isInstanceOf(InvalidCursorException.class);
            verify(bookRepository, never()).search(any(), any());
        }
    }

    @Nested
    class Delete {

        @Test
        void 논리_삭제는_deletedAt만_기록한다() {
            UUID id = UUID.randomUUID();
            Book book = book();
            given(bookRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(book));

            bookService.softDelete(id);

            assertThat(book.isDeleted()).isTrue();
            verify(bookRepository, never()).delete(any());
        }

        @Test
        void 물리_삭제는_논리_삭제된_도서도_지운다() {
            UUID id = UUID.randomUUID();
            Book book = book();
            book.softDelete(Instant.now());
            given(bookRepository.findById(id)).willReturn(Optional.of(book));

            bookService.hardDelete(id);

            ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
            verify(bookRepository).delete(captor.capture());
            assertThat(captor.getValue()).isSameAs(book);
        }

        @Test
        void 없는_도서를_물리_삭제하면_BookNotFoundException() {
            UUID id = UUID.randomUUID();
            given(bookRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.hardDelete(id)).isInstanceOf(BookNotFoundException.class);
        }
    }
}
