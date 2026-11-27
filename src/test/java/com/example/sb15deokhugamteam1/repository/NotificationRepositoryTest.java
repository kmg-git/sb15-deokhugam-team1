package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.config.PostgresTestConfig;
import com.example.sb15deokhugamteam1.dto.response.NotificationDto;
import com.example.sb15deokhugamteam1.entity.Book;
import com.example.sb15deokhugamteam1.entity.Notification;
import com.example.sb15deokhugamteam1.entity.Review;
import com.example.sb15deokhugamteam1.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestConfig.class)
@Transactional
public class NotificationRepositoryTest {

    private static final Instant T1 = Instant.parse("2026-11-26T01:00:00Z");
    private static final Instant T2 = Instant.parse("2026-11-26T02:00:00Z");
    private static final Instant T3 = Instant.parse("2026-11-26T03:00:00Z");
    private static final Instant NOW = Instant.parse("2030-01-01T00:00:00Z");
    private static final int LIMIT = 10;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    EntityManager entityManager;

    private User owner;
    private User other;
    private Review firstReview;
    private Review secondReview;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("owner@test.com", "소유자", "password"));
        other = userRepository.save(new User("other@test.com", "다른사람", "password"));
        Book book = bookRepository.save(
                new Book("제목", "저자", "설명", "출판사", LocalDate.of(2026, 1, 1), null, null));
        firstReview = reviewRepository.save(new Review(book, owner, "첫 번째 리뷰", 5));
        secondReview = reviewRepository.save(new Review(book, owner, "두 번째 리뷰", 4));
    }

    @Test
    @DisplayName("본인의 알림만 최신순으로 조회하고 리뷰 내용을 함께 채운다")
    void findsOwnNotificationsNewestFirst() {
        Notification first = saveNotification(owner, firstReview, "첫 번째 알림", T1);
        Notification second = saveNotification(owner, secondReview, "두 번째 알림", T2);
        Notification third = saveNotification(owner, firstReview, "세 번째 알림", T3);
        saveNotification(other, firstReview, "남의 알림", T2);

        List<NotificationDto> page =
                notificationRepository.findPage(owner.getId(), Sort.Direction.DESC, null, null, LIMIT);

        assertThat(page).extracting(NotificationDto::id)
                .containsExactly(third.getId(), second.getId(), first.getId());
        assertThat(page).extracting(NotificationDto::reviewContent)
                .containsExactly("첫 번째 리뷰", "두 번째 리뷰", "첫 번째 리뷰");
    }

    @Test
    @DisplayName("오름차순이면 오래된 알림부터 조회한다")
    void findsOldestFirstWhenAscending() {
        Notification first = saveNotification(owner, firstReview, "첫 번째 알림", T1);
        Notification second = saveNotification(owner, secondReview, "두 번째 알림", T2);

        List<NotificationDto> page =
                notificationRepository.findPage(owner.getId(), Sort.Direction.ASC, null, null, LIMIT);

        assertThat(page).extracting(NotificationDto::id)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("limit보다 하나 더 조회해서 다음 페이지가 있는지 알 수 있게 한다")
    void fetchesOneMoreThanLimit() {
        saveNotification(owner, firstReview, "첫 번째 알림", T1);
        saveNotification(owner, firstReview, "두 번째 알림", T2);
        saveNotification(owner, firstReview, "세 번째 알림", T3);

        List<NotificationDto> page =
                notificationRepository.findPage(owner.getId(), Sort.Direction.DESC, null, null, 2);

        assertThat(page).hasSize(3);
    }

    @Test
    @DisplayName("커서 다음 알림부터 이어서 조회한다")
    void continuesAfterCursor() {
        Notification first = saveNotification(owner, firstReview, "첫 번째 알림", T1);
        Notification second = saveNotification(owner, firstReview, "두 번째 알림", T2);
        saveNotification(owner, firstReview, "세 번째 알림", T3);

        List<NotificationDto> page = notificationRepository.findPage(
                owner.getId(), Sort.Direction.DESC, second.getId(), T2, LIMIT);

        assertThat(page).extracting(NotificationDto::id).containsExactly(first.getId());
    }

    @Test
    @DisplayName("생성 시각이 같은 알림도 id 기준으로 빠짐없이 이어서 조회한다")
    void continuesByIdWhenCreatedAtIsSame() {
        saveNotification(owner, firstReview, "알림 A", T1);
        saveNotification(owner, secondReview, "알림 B", T1);

        List<NotificationDto> firstPage =
                notificationRepository.findPage(owner.getId(), Sort.Direction.DESC, null, null, 1);
        NotificationDto head = firstPage.get(0);
        List<NotificationDto> nextPage = notificationRepository.findPage(
                owner.getId(), Sort.Direction.DESC, head.id(), head.createdAt(), 1);

        assertThat(firstPage).hasSize(2);
        assertThat(nextPage).extracting(NotificationDto::id).containsExactly(firstPage.get(1).id());
    }

    @Test
    @DisplayName("사용자의 알림 개수만 센다")
    void countsOwnNotifications() {
        saveNotification(owner, firstReview, "첫 번째 알림", T1);
        saveNotification(owner, secondReview, "두 번째 알림", T2);
        saveNotification(other, firstReview, "남의 알림", T1);

        long count = notificationRepository.countByUserId(owner.getId());

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("모두 확인하면 본인의 안 읽은 알림만 확인 처리하고 바뀐 개수를 돌려준다")
    void marksOnlyOwnUnreadAsConfirmed() {
        Notification unread1 = saveNotification(owner, firstReview, "안 읽음 1", T1);
        Notification unread2 = saveNotification(owner, secondReview, "안 읽음 2", T2);
        Notification alreadyConfirmed = saveNotification(owner, firstReview, "이미 읽음", T3);
        alreadyConfirmed.updateConfirmed(true);
        Notification othersUnread = saveNotification(other, firstReview, "남의 알림", T1);
        entityManager.flush();

        int updated = notificationRepository.markAllAsRead(owner.getId(), NOW);

        assertThat(updated).isEqualTo(2);
        assertThat(reload(unread1).getConfirmed()).isTrue();
        assertThat(reload(unread1).getUpdatedAt()).isEqualTo(NOW);
        assertThat(reload(unread2).getConfirmed()).isTrue();
        assertThat(reload(alreadyConfirmed).getUpdatedAt()).isNotEqualTo(NOW);
        assertThat(reload(othersUnread).getConfirmed()).isFalse();
    }

    // 정렬·커서를 확인하려면 생성 시각을 직접 정해야 해서, 저장 후 created_at을 바로 바꾼다
    private Notification saveNotification(User user, Review review, String message, Instant createdAt) {
        Notification notification = notificationRepository.saveAndFlush(Notification.create(user, review, message));
        entityManager.createNativeQuery("update notifications set created_at = :createdAt where id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", notification.getId())
                .executeUpdate();
        return notification;
    }

    private Notification reload(Notification notification) {
        return notificationRepository.findById(notification.getId()).orElseThrow();
    }
}
