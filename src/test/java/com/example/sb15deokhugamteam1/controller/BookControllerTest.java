package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.response.BookDto;
import com.example.sb15deokhugamteam1.exception.book.BookNotFoundException;
import com.example.sb15deokhugamteam1.exception.book.DuplicateIsbnException;
import com.example.sb15deokhugamteam1.exception.book.InvalidIsbnException;
import com.example.sb15deokhugamteam1.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

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
    void ISBN이_중복이면_409() throws Exception {
        given(bookService.create(any())).willThrow(new DuplicateIsbnException("9788966260959"));

        mockMvc.perform(multipart("/api/books").file(bookData(VALID_JSON)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOK_02"))
                .andExpect(jsonPath("$.details.isbn").value("9788966260959"))
                .andExpect(jsonPath("$.status").value(409));
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
