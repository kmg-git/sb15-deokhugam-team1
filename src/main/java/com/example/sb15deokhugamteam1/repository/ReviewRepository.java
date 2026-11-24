package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 삭제되지 않은 리뷰 단건 조회
    Optional<Review> findByIdAndDeletedAtIsNull(UUID id);

    // 특정 도서의 삭제되지 않은 리뷰 목록 조회
    Page<Review> findAllByBook_IdAndDeletedAtIsNull(
            UUID bookId,
            Pageable pageable
    );
}
