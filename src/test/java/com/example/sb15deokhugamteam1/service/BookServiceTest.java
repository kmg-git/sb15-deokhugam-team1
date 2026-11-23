package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.request.BookCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.BookUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.BookDto;
import com.example.sb15deokhugamteam1.entity.Book;
import com.example.sb15deokhugamteam1.exception.book.BookNotFoundException;
import com.example.sb15deokhugamteam1.exception.book.DuplicateIsbnException;
import com.example.sb15deokhugamteam1.exception.book.InvalidIsbnException;
import com.example.sb15deokhugamteam1.repository.BookRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
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
                    .willThrow(new DataIntegrityViolationException("uq_books_active_isbn"));

            assertThatThrownBy(() -> bookService.create(createRequest("9788966260959")))
                    .isInstanceOf(DuplicateIsbnException.class);
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
            book.softDelete(java.time.Instant.now());
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
