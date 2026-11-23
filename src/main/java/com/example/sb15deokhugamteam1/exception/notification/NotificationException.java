package com.example.sb15deokhugamteam1.exception.notification;

import com.example.sb15deokhugamteam1.exception.BaseException;

import java.util.Map;

public abstract class NotificationException extends BaseException {

    protected NotificationException(NotificationErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
