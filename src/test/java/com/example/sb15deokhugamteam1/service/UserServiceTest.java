package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.request.UserLoginRequest;
import com.example.sb15deokhugamteam1.dto.request.UserRegisterRequest;
import com.example.sb15deokhugamteam1.dto.response.UserDto;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.exception.user.DuplicateUserEmailException;
import com.example.sb15deokhugamteam1.exception.user.LoginFailedException;
import com.example.sb15deokhugamteam1.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest("test@test.com", "seok0630", "test1234!");

        given(userRepository.existsByEmail("test@test.com")).willReturn(true); // test@test.com 이 들어오면 이미 존재한다는 값을 주는거고

        assertThatThrownBy(() -> userService.register(userRegisterRequest)).isInstanceOf(DuplicateUserEmailException.class);
    }

    @Test
    @DisplayName(value = "정상 가입이면 UserDTO 돌려주기")
    void register_success() {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest("test@test.com", "seok0630", "test1234!");
        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserDto userDto = userService.register(userRegisterRequest);

        assertThat(userDto.email()).isEqualTo("test@test.com");
        assertThat(userDto.nickname()).isEqualTo("seok0630");
    }

    @Test
    @DisplayName(value = "비밀번호 암호화 검증")
    void encode_password() {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest("test@test.com", "seok0630", "test1234!");
        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        userService.register(userRegisterRequest);

        // passwordEncoder를 불러 인코딩된 raw 암호가 test1234! 였는지 검증
        verify(passwordEncoder).encode("test1234!");
    }


    @Test
    @DisplayName(value = "정상 로그인이면 UserDTO 돌려주기")
    void login_success() {
        UserLoginRequest userLoginRequest = new UserLoginRequest("test@test.com", "test1234!");

        User user = new User(userLoginRequest.email(), "seok0630", "hashed");
        given(userRepository.findByEmailAndDeletedAtIsNull(userLoginRequest.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches("test1234!", "hashed")).willReturn(true);

        UserDto userDto = userService.login(userLoginRequest);

        assertThat(userDto.email()).isEqualTo("test@test.com");
        assertThat(userDto.nickname()).isEqualTo("seok0630");
    }

    @Test
    @DisplayName(value = "없는 이메일이면 예외")
    void login_email() {
        UserLoginRequest userLoginRequest = new UserLoginRequest("test@test.com", "test1234!");

        given(userRepository.findByEmailAndDeletedAtIsNull(userLoginRequest.email())).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(userLoginRequest)).isInstanceOf(LoginFailedException.class);
    }

    @Test
    @DisplayName(value = "비밀번호가 틀리면 예외")
    void login_password() {
        UserLoginRequest userLoginRequest = new UserLoginRequest("test@test.com", "test1234!");

        User user = new User(userLoginRequest.email(), "seok0630", "hashed");
        given(userRepository.findByEmailAndDeletedAtIsNull(userLoginRequest.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches("test1234!", "hashed")).willReturn(false);

        assertThatThrownBy(() -> userService.login(userLoginRequest)).isInstanceOf(LoginFailedException.class);
    }
}
