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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    private static final UUID REVIEW_ID = UUID.randomUUID();
    private static final UUID AUTHOR_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final String NICKNAME = "닉네임";
    private static final int RANK = 3;

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    User author;

    @Mock
    Review review;

    @InjectMocks
    NotificationService notificationService;

    @BeforeEach
    void setUp() {
        given(userRepository.getReferenceById(AUTHOR_ID)).willReturn(author);
        given(reviewRepository.getReferenceById(REVIEW_ID)).willReturn(review);
    }

    @Test
    @DisplayName("좋아요 이벤트를 받으면 리뷰 작성자에게 좋아요 알림을 보냄")
    void notificationLike() {
        notificationService.notifyLike(
                new ReviewLikedEvent(REVIEW_ID, AUTHOR_ID, ACTOR_ID, NICKNAME)
        );

        Notification notification = captureSaved();
        assertThat(notification.getUser()).isSameAs(author);
        assertThat(notification.getReview()).isSameAs(review);
        assertThat(notification.getMessage()).isEqualTo("닉네임님이 리뷰를 좋아합니다.");
        assertThat(notification.getConfirmed()).isFalse();
    }

    @Test
    @DisplayName("댓글 이벤트를 받으면 리뷰 작성자에게 댓글 알림을 보냄")
    void notificationComment() {
        notificationService.notifyComment(
                new CommentCreatedEvent(REVIEW_ID, AUTHOR_ID, ACTOR_ID, NICKNAME)
        );

        Notification notification = captureSaved();
        assertThat(notification.getUser()).isSameAs(author);
        assertThat(notification.getReview()).isSameAs(review);
        assertThat(notification.getMessage()).isEqualTo("닉네임님이 리뷰에 댓글을 남겼습니다.");
        assertThat(notification.getConfirmed()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"DAILY, 일간", "WEEKLY, 주간", "MONTHLY, 월간", "ALL_TIME, 역대"})
    @DisplayName("인기 리뷰 이벤트를 받으면 기간 이름과 순위가 들어간 알림을 받음")
    void notificationRank(Period period, String periodName) {
        notificationService.notifyRank(
                new PopularReviewRankedEvent(REVIEW_ID, AUTHOR_ID, period, RANK)
        );

        Notification notification = captureSaved();
        assertThat(notification.getUser()).isSameAs(author);
        assertThat(notification.getReview()).isSameAs(review);
        assertThat(notification.getMessage()).isEqualTo("나의 리뷰가 " + periodName + " 인기 리뷰 3위에 선정되었습니다.");
        assertThat(notification.getConfirmed()).isFalse();
    }

    private Notification captureSaved() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        return captor.getValue();
    }
}
