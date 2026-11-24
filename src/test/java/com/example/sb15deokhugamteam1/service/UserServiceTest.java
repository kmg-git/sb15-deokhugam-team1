package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.request.UserRegisterRequest;
import com.example.sb15deokhugamteam1.exception.user.DuplicateUserEmailException;
import com.example.sb15deokhugamteam1.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    void 이미_있는_이메일() {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest("test@test.com", "seok0630", "zz1245");

        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        Assertions.assertThatThrownBy(() -> userService.register(userRegisterRequest)).isInstanceOf(DuplicateUserEmailException.class);
    }
}
