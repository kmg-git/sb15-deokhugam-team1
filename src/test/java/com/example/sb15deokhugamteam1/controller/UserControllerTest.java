package com.example.sb15deokhugamteam1.controller;

import com.example.sb15deokhugamteam1.dto.response.UserDto;
import com.example.sb15deokhugamteam1.exception.user.DuplicateUserEmailException;
import com.example.sb15deokhugamteam1.exception.user.LoginFailedException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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


}
