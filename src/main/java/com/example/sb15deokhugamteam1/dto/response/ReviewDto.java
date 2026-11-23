package com.example.sb15deokhugamteam1.dto.response;


import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
    public class ReviewDto {

        // 리뷰 고유 ID
        private final UUID id;

        // 도서 정보
        private final UUID bookId;
        private final String bookTitle;
        private final String bookThumbnailUrl;

        // 작성자 정보
        private final UUID userId;
        private final String userNickname;

        // 리뷰 내용
        private final String content;
        private final Integer rating;

        // 리뷰 통계
        private final int likeCount;
        private final int commentCount;

        // 현재 요청자의 좋아요 여부
        private final boolean likedByMe;

        // 생성 및 수정 시간
        private final Instant createdAt;
        private final Instant updatedAt;

        public ReviewDto(
                UUID id,
                UUID bookId,
                String bookTitle,
                String bookThumbnailUrl,
                UUID userId,
                String userNickname,
                String content,
                Integer rating,
                int likeCount,
                int commentCount,
                boolean likedByMe,
                Instant createdAt,
                Instant updatedAt
        ) {
            this.id = id;
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.bookThumbnailUrl = bookThumbnailUrl;
            this.userId = userId;
            this.userNickname = userNickname;
            this.content = content;
            this.rating = rating;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.likedByMe = likedByMe;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;

        }


    }


