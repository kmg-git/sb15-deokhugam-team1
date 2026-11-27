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
import com.example.sb15deokhugamteam1.mapper.dto.UserMapper;
import com.example.sb15deokhugamteam1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto register(UserRegisterRequest userRegisterRequest) {
        // 이메일 중복 검사. 중복이면 DuplicateUserEmailException을 던진다.
        if (userRepository.existsByEmail(userRegisterRequest.email())) {
            throw new DuplicateUserEmailException(userRegisterRequest.email());
        }

        // 저장소에 저장 후 돌려받은 변수를 돌려줘야 id랑 createdAt이 생겨있음
        User user = userRepository.save(
                new User(
                    userRegisterRequest.email(),
                    userRegisterRequest.nickname(),
                    passwordEncoder.encode(userRegisterRequest.password())
                )
        );

        return UserMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto login(UserLoginRequest userLoginRequest) {
        // 이메일이 없어 user가 빈 경우. 로그인 실패
        User user = userRepository.findByEmailAndDeletedAtIsNull(userLoginRequest.email())
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(userLoginRequest.password(), user.getPassword())) {
            throw new LoginFailedException();
        }

        return UserMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto find(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return UserMapper.toDto(user);
    }

    @Transactional
    public UserDto update(UUID userId, UUID requestUserId, UserUpdateRequest userUpdateRequest) {
        // 본인만 수정할 수 있다.
        if (!userId.equals(requestUserId)) {
            throw new UserForbiddenException(userId);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateNickname(userUpdateRequest.nickname());

        return UserMapper.toDto(user);
    }

    @Transactional
    public void softDelete(UUID userId, UUID requestUserId) {
        // 본인만 삭제할 수 있다.
        if (!userId.equals(requestUserId)) {
            throw new UserForbiddenException(userId);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.softDelete();
    }

    @Transactional
    public void hardDelete(UUID userId, UUID requestUserId) {
        // 본인만 삭제할 수 있다.
        if (!userId.equals(requestUserId)) {
            throw new UserForbiddenException(userId);
        }

        // 논리 삭제된 유저를 찾아야 하므로
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        userRepository.delete(user);
    }
}
