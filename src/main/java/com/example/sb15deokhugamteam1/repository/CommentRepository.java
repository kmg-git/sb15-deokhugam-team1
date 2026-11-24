package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
