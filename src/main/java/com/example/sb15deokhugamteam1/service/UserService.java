package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.dto.request.UserRegisterRequest;
import com.example.sb15deokhugamteam1.dto.response.UserDto;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.exception.user.DuplicateUserEmailException;
import com.example.sb15deokhugamteam1.mapper.dto.UserMapper;
import com.example.sb15deokhugamteam1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
