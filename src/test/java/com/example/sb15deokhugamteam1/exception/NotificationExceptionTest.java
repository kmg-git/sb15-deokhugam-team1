package com.example.sb15deokhugamteam1.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationExceptionTest {

    private static final String NOT_FOUND_CODE = "NOTIFICATION_01";
    private static final String FORBIDDEN_CODE = "NOTIFICATION_02";


    @Test
    @DisplayName("알림을 찾지 못하면 404와 NOTIFICATION_01 코드를 갖고 알림 ID를 details에 담는다.")
    void hasNotFoundCode() {
        UUID notificationId = UUID.randomUUID();

        NotificationNotFoundException exception = new NotificationNotFoundException(notificationId);

        assertThat(exception.getErrorCode().getStatus().isEqualTo(HttpStatus.NOT_FOUND));
        assertThat(exception.getErrorCode().getCode()).isEqualTo(NOT_FOUND_CODE);
        assertThat(exception.getDetails()).containsEntry("notificationId", notificationId);
    }

    @Test
    @DisplayName("다른 사람의 알림이면 403과 NOTIFICATION_02 코드를 갖고 알림 ID와 사용자 ID를 details에 담는다")
    void hasForbiddenCode() {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        NotificationForbiddenException exception = new NotificationForbiddenException(notificationId, userId);

        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(exception.getErrorCode().getCode()).isEqualTo(FORBIDDEN_CODE);
        assertThat(exception.getDetails())
                .containsEntry("notificationId", notificationId)
                .containsEntry("userId", userId);
    }
}
