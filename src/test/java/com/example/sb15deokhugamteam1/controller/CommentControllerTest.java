package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.request.CommentCreateRequest;
import com.example.sb15deokhugamteam1.dto.request.CommentUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.CommentDto;
import com.example.sb15deokhugamteam1.service.CommentService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    private static final String COMMENTS_URL = "/api/comments";
    private static final String COMMENT_URL = COMMENTS_URL + "/{commentId}";
    private static final String REQUEST_USER_ID = "Deokhugam-Request-User-ID";
    private static final UUID REVIEW_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CommentService commentService;

    @Test
    @DisplayName("리퀘스트 문제 없음")
    void 크리에이트_성공_01() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest(REVIEW_ID, USER_ID, "댓글");
        given(commentService.create(request)).willReturn(commentDto(request.content()));

        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewId":"%s","userId":"%s","content":"%s"}
                                """.formatted(REVIEW_ID, USER_ID, request.content())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(COMMENT_ID.toString()))
                .andExpect(jsonPath("$.reviewId").value(REVIEW_ID.toString()))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.content").value(request.content()));
        verify(commentService).create(request);
    }

    @Test
    @DisplayName("UUID 양식 다름")
    void 크리에이트_실패_01() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewId":"invalid-uuid","userId":"%s","content":"댓글"}
                                """.formatted(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_01"));
        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("reviewId + userId 누락 response 디테일 2개")
    void 크리에이트_실패_02() throws Exception {
        mockMvc.perform(post(COMMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"댓글"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_01"))
                .andExpect(jsonPath("$.details.length()").value(2))
                .andExpect(jsonPath("$.details.reviewId").exists())
                .andExpect(jsonPath("$.details.userId").exists());
        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("리퀘스트 문제 없음")
    void 단건_조회_성공_01() throws Exception {
        given(commentService.find(COMMENT_ID)).willReturn(commentDto("댓글"));

        mockMvc.perform(get(COMMENT_URL, COMMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COMMENT_ID.toString()))
                .andExpect(jsonPath("$.content").value("댓글"));
        verify(commentService).find(COMMENT_ID);
    }

    @Test
    @DisplayName("리퀘스트 문제 없음")
    void 업데이트_성공_01() throws Exception {
        CommentUpdateRequest request = new CommentUpdateRequest("수정한 댓글");
        given(commentService.update(COMMENT_ID, USER_ID, request)).willReturn(commentDto(request.content()));

        mockMvc.perform(patch(COMMENT_URL, COMMENT_ID)
                        .header(REQUEST_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"%s"}
                                """.formatted(request.content())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COMMENT_ID.toString()))
                .andExpect(jsonPath("$.content").value(request.content()));
        verify(commentService).update(COMMENT_ID, USER_ID, request);
    }

    @Test
    @DisplayName("NotBlank가 null임")
    void 업데이트_실패_01() throws Exception {
        mockMvc.perform(patch(COMMENT_URL, COMMENT_ID)
                        .header(REQUEST_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_01"))
                .andExpect(jsonPath("$.details.content").exists());
        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("NotBlank가 blank임")
    void 업데이트_실패_02() throws Exception {
        mockMvc.perform(patch(COMMENT_URL, COMMENT_ID)
                        .header(REQUEST_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_01"))
                .andExpect(jsonPath("$.details.content").exists());
        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("리퀘스트 문제 없음")
    void 논리삭제_성공_01() throws Exception {
        mockMvc.perform(delete(COMMENT_URL, COMMENT_ID).header(REQUEST_USER_ID, USER_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(commentService).softDelete(COMMENT_ID, USER_ID);
    }

    @Test
    @DisplayName("commentId(@PathVariable) 누락")
    void 논리삭제_실패_01() throws Exception {
        mockMvc.perform(delete(COMMENTS_URL).header(REQUEST_USER_ID, USER_ID))
                .andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("requesterId(@RequestHeader) 누락")
    void 논리삭제_실패_02() throws Exception {
        mockMvc.perform(delete(COMMENT_URL, COMMENT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_01"));
        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("리퀘스트 문제 없음")
    void 물리삭제_성공_01() throws Exception {
        mockMvc.perform(delete(COMMENT_URL + "/hard", COMMENT_ID).header(REQUEST_USER_ID, USER_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(commentService).hardDelete(COMMENT_ID, USER_ID);
    }

    private static CommentDto commentDto(String content) {
        Instant timestamp = Instant.parse("2026-11-27T00:00:00Z");
        return new CommentDto(COMMENT_ID, REVIEW_ID, USER_ID, "작성자", content, timestamp, timestamp);
    }
}
