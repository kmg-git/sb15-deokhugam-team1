package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Review;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;
import java.util.UUID;



public interface ReviewRepository extends JpaRepository<Review, UUID>{

    // 삭제되지 않은 리뷰 단건 조회
    Optional<Review> findByIdAndDeletedAtIsNull(UUID id);

    // 특정 도서의 삭제되지 않은 리뷰 목록 조회
    Page<Review> findAllByBook_IdAndDeletedAtIsNull(
            UUID bookId,
            Pageable pageable
    );

    // 좋아요 처리를 위한 리뷰 쓰기 잠금 조회
    @Lock(LockModeType.WRITE)
    @Query("SELECT r from  Review r WHERE r.id = :reviewId AND r.deletedAt IS NULL")
    Optional<Review> findByIdAndDeletedAtIsNullForUpdate(
            @Param("reviewId") UUID reviewId
    );
}
