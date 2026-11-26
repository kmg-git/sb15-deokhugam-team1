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

    // 댓글 서비스에서 리뷰의 댓글 수를 갱신할 때 사용한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Review r where r.id = :id")
    Optional<Review> findByIdForUpdate(@Param("id") UUID id);

    // 삭제되지 않은 리뷰 단건 조회
    Optional<Review> findByIdAndDeletedAtIsNull(UUID id);

    // 특정 도서의 삭제되지 않은 리뷰 목록 조회
    Page<Review> findAllByBook_IdAndDeletedAtIsNull(
            UUID bookId,
            Pageable pageable
    );
}
