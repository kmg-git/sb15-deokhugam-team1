package com.example.sb15deokhugamteam1.mapper.dto;

import com.example.sb15deokhugamteam1.dto.response.CommentDto;
import com.example.sb15deokhugamteam1.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "reviewId", source = "review.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userNickname", source = "user.nickname")
    CommentDto toDto(Comment comment);

}
