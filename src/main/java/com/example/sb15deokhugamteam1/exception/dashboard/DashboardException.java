package com.example.sb15deokhugamteam1.exception.dashboard;

import com.example.sb15deokhugamteam1.exception.BaseException;

import java.util.Map;

public abstract class DashboardException extends BaseException {

    protected DashboardException(DashboardErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
