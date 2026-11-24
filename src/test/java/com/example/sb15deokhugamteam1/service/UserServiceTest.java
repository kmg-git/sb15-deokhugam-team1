package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.request.UserRegisterRequest;
import com.example.sb15deokhugamteam1.dto.response.UserDto;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.exception.user.DuplicateUserEmailException;
import com.example.sb15deokhugamteam1.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName(value = "이미 있는 이메일")
    void verify_email() {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest("test@test.com", "seok0630", "zz1245");

        given(userRepository.existsByEmail("test@test.com")).willReturn(true); // test@test.com 이 들어오면 이미 존재한다는 값을 주는거고

        assertThatThrownBy(() -> userService.register(userRegisterRequest)).isInstanceOf(DuplicateUserEmailException.class);
    }

    @Test
    @DisplayName(value = "정상 가입이면 UserDTO 돌려주기")
    void register_success() {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest("test@test.com", "seok0630", "zz1245");
        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserDto userDto = userService.register(userRegisterRequest);

        assertThat(userDto.email()).isEqualTo("test@test.com");
        assertThat(userDto.nickname()).isEqualTo("seok0630");
    }
}
