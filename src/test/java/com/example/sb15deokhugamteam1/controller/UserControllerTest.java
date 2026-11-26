package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.response.UserDto;
import com.example.sb15deokhugamteam1.exception.user.DuplicateUserEmailException;
import com.example.sb15deokhugamteam1.exception.user.LoginFailedException;
import com.example.sb15deokhugamteam1.exception.user.UserForbiddenException;
import com.example.sb15deokhugamteam1.exception.user.UserNotFoundException;
import com.example.sb15deokhugamteam1.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.time.Instant;
import java.util.UUID;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    UserDto userDto = new UserDto(UUID.randomUUID(), "test@test.com", "tester", Instant.now());

    @Test
    void 등록_성공하면_201() throws Exception {
        given(userService.register(any())).willReturn(userDto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"test@test.com","nickname":"tester","password":"test1234!"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    void 이메일_형식이_틀리면_400() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"test","nickname":"tester","password":"test1234!"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 비밀번호_규칙이_안맞으면_400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"test@test.com","nickname":"tester","password":"zz1245"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 이미_있는_이메일이면_409() throws Exception {
        given(userService.register(any())).willThrow(new DuplicateUserEmailException("test@test.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"test@test.com","nickname":"tester1","password":"test1234!"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_01"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));
    }


    /** login 테스트 **/

    @Test
    void 로그인_성공하면_200() throws Exception {
        given(userService.login(any())).willReturn(userDto);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"test@test.com", "password":"test1234!"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void 로그인_실패하면_401() throws Exception {
        given(userService.login(any())).willThrow(new LoginFailedException());

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"test@test.com","password":"test1234!"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_01"))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 로그인_이메일_형식이_틀리면_400() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"test", "password":"test1234!"}
                        """))
                .andExpect(status().isBadRequest());
    }

    /** 상세 조회 테스트 **/

    @Test
    void 조회_성공하면_200() throws Exception {
        given(userService.find(userDto.id())).willReturn(userDto);

        mockMvc.perform(get("/api/users/{userId}", userDto.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    void 없는_유저면_404() throws Exception {
        UUID userId = UUID.randomUUID();
        given(userService.find(userId)).willThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_02"));
    }

    /** 닉네임 수정 테스트 **/

    @Test
    void 수정_성공하면_200() throws Exception {
        UUID userId = userDto.id();
        UserDto updated = new UserDto(userId, "test@test.com", "newnick", Instant.now());
        given(userService.update(any(), any(), any())).willReturn(updated);

        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .header("Deokhugam-Request-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"nickname":"newnick"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("newnick"));
    }

    @Test
    void 수정_닉네임이_한글자면_400() throws Exception {
        UUID userId = userDto.id();

        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .header("Deokhugam-Request-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"nickname":"a"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 남의_닉네임을_수정하면_403() throws Exception {
        UUID userId = userDto.id();
        given(userService.update(any(), any(), any())).willThrow(new UserForbiddenException(userId));

        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .header("Deokhugam-Request-User-ID", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"nickname":"newnick"}
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_03"));
    }

    @Test
    void 없는_유저를_수정하면_404() throws Exception {
        UUID userId = UUID.randomUUID();
        given(userService.update(any(), any(), any())).willThrow(new UserNotFoundException(userId));

        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .header("Deokhugam-Request-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"nickname":"newnick"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_02"));
    }


}
