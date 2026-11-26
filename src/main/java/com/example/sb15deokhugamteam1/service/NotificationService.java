package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.entity.Notification;
import com.example.sb15deokhugamteam1.entity.Period;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.User;
import com.example.sb15deokhugamteam1.event.CommentCreatedEvent;
import com.example.sb15deokhugamteam1.event.PopularReviewRankedEvent;
import com.example.sb15deokhugamteam1.event.ReviewLikedEvent;
import com.example.sb15deokhugamteam1.repository.NotificationRepository;
import com.example.sb15deokhugamteam1.repository.ReviewRepository;
import com.example.sb15deokhugamteam1.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String LIKE_MESSAGE = "%s님이 나의 리뷰를 좋아합니다.";
    private static final String COMMENT_MESSAGE = "%s님이 나의 리뷰에 댓글을 남겼습니다.";
    private static final String POPULAR_RANK_MESSAGE = "나의 리뷰가 %s 인기 리뷰 %d위에 선정되었습니다.";

    private static final String DAILY_NAME = "일간";
    private static final String WEEKLY_NAME = "주간";
    private static final String MONTHLY_NAME = "월간";
    private static final String ALL_TIME_NAME = "역대";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void notifyLike(ReviewLikedEvent event) {
        String message = LIKE_MESSAGE.formatted(event.likerNickname());
        save(event.reviewAuthorId(), event.reviewId(), message);
    }

    @Transactional
    public void notifyComment(CommentCreatedEvent event) {
        String message = COMMENT_MESSAGE.formatted(event.commenterNickname()());
        save(event.reviewAuthorId(), event.reviewId, message);
    }

    @Transactional
    public void notifyRank(PopularReviewRankedEvent event) {
        String message = POPULAR_RANK_MESSAGE.formatted(periodName(event.period()), event.rank());
        save(event.reviewAuthorId(), event.reviewId(), message);
    }

    private void save(UUID receiverId, UUID reviewId, String message) {
        User receiver = userRepository.getReferenceById(receiverId);
        Review review = reviewRepository.getReferenceById(reviewId);
        notificationRepository.save(Notification.create(receiver, review, message));
        log.info("알림 생성: userId={}, reviewId={}", receiverId, reviewId);
    }

    private String periodName(Period period) {
        return switch (period) {
            case DAILY -> DAILY_NAME;
            case WEEKLY -> WEEKLY_NAME;
            case MONTHLY -> MONTHLY_NAME;
            case ALL_TIME -> ALL_TIME_NAME;
        };
    }

}
