package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // 논리 삭제되지 않은 유저를 ID로 찾는다
    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    // 이메일 중복 검사
    boolean existsByEmail(String email);

    // 논리 삭제되지 않은 유저를 Email로 찾는다
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
