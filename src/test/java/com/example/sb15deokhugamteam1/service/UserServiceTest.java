package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.request.UserLoginRequest;
import com.example.sb15deokhugamteam1.dto.request.UserRegisterRequest;
import com.example.sb15deokhugamteam1.dto.request.UserUpdateRequest;
import com.example.sb15deokhugamteam1.dto.response.UserDto;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.exception.user.DuplicateUserEmailException;
import com.example.sb15deokhugamteam1.exception.user.LoginFailedException;
import com.example.sb15deokhugamteam1.exception.user.UserForbiddenException;
import com.example.sb15deokhugamteam1.exception.user.UserNotFoundException;
import com.example.sb15deokhugamteam1.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

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


    /** Login 테스트 **/

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

    /** 상세 조회 테스트 **/

    @Test
    @DisplayName(value = "있는 유저면 UserDTO 돌려주기")
    void find_success() {
        UUID userId = UUID.randomUUID();
        User user = new User("test@test.com", "seok0630", "hashed");
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        UserDto userDto = userService.find(userId);

        assertThat(userDto.email()).isEqualTo("test@test.com");
        assertThat(userDto.nickname()).isEqualTo("seok0630");
    }

    @Test
    @DisplayName(value = "없는 유저면 예외")
    void find_not_found() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.find(userId)).isInstanceOf(UserNotFoundException.class);
    }

    /** 닉네임 수정 테스트 **/

    @Test
    @DisplayName(value = "본인이면 닉네임이 바뀐 UserDTO 돌려주기")
    void update_success() {
        UUID userId = UUID.randomUUID();
        User user = new User("test@test.com", "seok0630", "hashed");
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        UserDto userDto = userService.update(userId, userId, new UserUpdateRequest("newnick"));

        assertThat(userDto.nickname()).isEqualTo("newnick");
    }

    @Test
    @DisplayName(value = "남의 닉네임을 수정하면 예외")
    void update_forbidden() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.update(userId, otherUserId, new UserUpdateRequest("newnick")))
                .isInstanceOf(UserForbiddenException.class);
    }

    @Test
    @DisplayName(value = "없는 유저의 닉네임을 수정하면 예외")
    void update_not_found() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(userId, userId, new UserUpdateRequest("newnick")))
                .isInstanceOf(UserNotFoundException.class);
    }
}
