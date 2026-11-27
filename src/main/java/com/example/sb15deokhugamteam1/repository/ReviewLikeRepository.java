
package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    // 특정 사용자가 특정 리뷰에 좋아요를 눌렀는지 조회
    Optional<ReviewLike> findByReview_IdAndUser_Id(
            UUID reviewId,
            UUID userId
    );

    // 특정 리뷰에 대한 특정 사용자의 좋아요 개수 조회
    long countByReview_IdAndUser_Id(
            UUID reviewId,
            UUID userId
    );

    // 특정 사용자가 여러 리뷰에 좋아요를 눌렀는지 한 번에 조회
    List<ReviewLike> findAllByUser_IdAndReview_IdIn(
            UUID userId,
            Collection<UUID> reviewIds
    );

    // 특정 리뷰에 연결된 좋아요 전체 삭제
    @Modifying
    @Query("DELETE FROM ReviewLike rl WHERE rl.review.id = :reviewId")
    void deleteAllByReviewId(@Param("reviewId") UUID reviewId);
}
