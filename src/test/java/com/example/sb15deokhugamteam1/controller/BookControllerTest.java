package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.page.CursorPageResponseBookDto;
import com.example.sb15deokhugamteam1.dto.request.BookSearchCondition;
import com.example.sb15deokhugamteam1.dto.request.BookSortField;
import com.example.sb15deokhugamteam1.dto.response.BookDto;
import com.example.sb15deokhugamteam1.exception.book.BookNotFoundException;
import com.example.sb15deokhugamteam1.exception.book.DuplicateIsbnException;
import com.example.sb15deokhugamteam1.exception.book.InvalidIsbnException;
import com.example.sb15deokhugamteam1.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    private static final String VALID_JSON = """
            {"title":"클린 코드","author":"로버트 C. 마틴","description":"설명",
             "publisher":"인사이트","publishedDate":"2013-12-24","isbn":"9788966260959"}
            """;

    private static MockMultipartFile bookData(String json) {
        return new MockMultipartFile("bookData", "", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8));
    }

    private static BookDto dto(UUID id) {
        return new BookDto(id, "클린 코드", "로버트 C. 마틴", "설명", "인사이트",
                LocalDate.of(2013, 12, 24), "9788966260959", null, 0, 0.0, Instant.now(), Instant.now());
    }

    @Test
    void 등록_성공하면_201() throws Exception {
        UUID id = UUID.randomUUID();
        given(bookService.create(any())).willReturn(dto(id));

        mockMvc.perform(multipart("/api/books").file(bookData(VALID_JSON)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.isbn").value("9788966260959"));
    }

    @Test
    void 제목이_공백이면_400이고_서비스를_호출하지_않는다() throws Exception {
        String json = VALID_JSON.replace("\"클린 코드\"", "\"   \"");

        mockMvc.perform(multipart("/api/books").file(bookData(json)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_01"))
                .andExpect(jsonPath("$.details.title").exists());
        verify(bookService, never()).create(any());
    }

    @Test
    void 제목이_255자를_넘으면_400() throws Exception {
        String json = VALID_JSON.replace("\"클린 코드\"", "\"" + "가".repeat(256) + "\"");

        mockMvc.perform(multipart("/api/books").file(bookData(json)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_01"))
                .andExpect(jsonPath("$.details.title").exists());
        verify(bookService, never()).create(any());
    }

    @Test
    void ISBN이_중복이면_409() throws Exception {
        given(bookService.create(any())).willThrow(new DuplicateIsbnException("9788966260959"));

        mockMvc.perform(multipart("/api/books").file(bookData(VALID_JSON)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOK_02"))
                .andExpect(jsonPath("$.details.isbn").value("9788966260959"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void 도서_상세_조회는_200() throws Exception {
        UUID id = UUID.randomUUID();
        given(bookService.find(id)).willReturn(dto(id));

        mockMvc.perform(get("/api/books/{bookId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("클린 코드"));
    }

    @Test
    void 없는_도서_조회는_404() throws Exception {
        UUID id = UUID.randomUUID();
        given(bookService.find(id)).willThrow(new BookNotFoundException(id));

        mockMvc.perform(get("/api/books/{bookId}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_01"))
                .andExpect(jsonPath("$.message").value("도서를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.details.bookId").value(id.toString()));
    }

    @Test
    void ISBN_형식이_틀리면_400과_BOOK_03() throws Exception {
        given(bookService.create(any())).willThrow(new InvalidIsbnException("123"));

        mockMvc.perform(multipart("/api/books").file(bookData(VALID_JSON)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BOOK_03"))
                .andExpect(jsonPath("$.details.isbn").value("123"));
    }

    @Test
    void 목록_조회는_파라미터가_없으면_제목_내림차순_50개() throws Exception {
        given(bookService.search(any())).willReturn(
                new CursorPageResponseBookDto(List.of(), null, null, 0, 0L, false));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.content").isArray());

        ArgumentCaptor<BookSearchCondition> captor = ArgumentCaptor.forClass(BookSearchCondition.class);
        verify(bookService).search(captor.capture());
        assertThat(captor.getValue().sortField()).isEqualTo(BookSortField.TITLE);
        assertThat(captor.getValue().direction()).isEqualTo(Sort.Direction.DESC);
        assertThat(captor.getValue().limit()).isEqualTo(50);
    }

    @Test
    void 목록_조회_파라미터를_그대로_넘긴다() throws Exception {
        given(bookService.search(any())).willReturn(
                new CursorPageResponseBookDto(List.of(), null, null, 0, 0L, false));

        mockMvc.perform(get("/api/books")
                        .param("keyword", "자바").param("orderBy", "rating").param("direction", "ASC")
                        .param("cursor", "4.5").param("after", "2026-11-01T00:00:00Z").param("limit", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<BookSearchCondition> captor = ArgumentCaptor.forClass(BookSearchCondition.class);
        verify(bookService).search(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new BookSearchCondition("자바", BookSortField.RATING,
                Sort.Direction.ASC, "4.5", Instant.parse("2026-11-01T00:00:00Z"), 10));
    }

    @Test
    void 지원하지_않는_정렬_기준이면_400과_BOOK_04() throws Exception {
        mockMvc.perform(get("/api/books").param("orderBy", "price"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BOOK_04"))
                .andExpect(jsonPath("$.details.orderBy").value("price"));
        verify(bookService, never()).search(any());
    }

    @Test
    void limit이_범위를_벗어나거나_direction이_틀리면_400() throws Exception {
        mockMvc.perform(get("/api/books").param("limit", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/books").param("limit", "101")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/books").param("direction", "UP")).andExpect(status().isBadRequest());
        verify(bookService, never()).search(any());
    }

    @Test
    void 수정은_multipart_PATCH로_받는다() throws Exception {
        UUID id = UUID.randomUUID();
        given(bookService.update(eq(id), any())).willReturn(dto(id));

        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/books/{bookId}", id).file(bookData(VALID_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("클린 코드"));
    }

    @Test
    void 논리_삭제와_물리_삭제는_204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/books/{bookId}", id)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/books/{bookId}/hard", id)).andExpect(status().isNoContent());

        verify(bookService).softDelete(id);
        verify(bookService).hardDelete(id);
    }

    @Test
    void 이미_삭제된_도서를_삭제하면_404() throws Exception {
        UUID id = UUID.randomUUID();
        willThrow(new BookNotFoundException(id)).given(bookService).softDelete(id);

        mockMvc.perform(delete("/api/books/{bookId}", id)).andExpect(status().isNotFound());
    }
}
