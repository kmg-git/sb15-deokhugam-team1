package com.example.sb15deokhugamteam1.mapper.dto;

import com.example.sb15deokhugamteam1.dto.response.UserDto;
import com.example.sb15deokhugamteam1.entity.User;

public class UserMapper {

    private UserMapper() {

    }

    public static UserDto toDto(User user) {
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getCreatedAt()
        );
    }
}
