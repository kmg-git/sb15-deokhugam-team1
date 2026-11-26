package com.example.sb15deokhugamteam1.exception.notification;

import java.util.Map;
import java.util.UUID;

public class NotificationForbiddenException extends NotificationException{

    public NotificationForbiddenException(UUID notificationId, UUID userId) {
        super(NotificationErrorCode.NOTIFICATION_FORBIDDEN, Map.of("notificationId", notificationId, "userId", userId))
    }
}
