package com.example.sb15deokhugamteam1.exception.user;

import java.util.Map;

public class LoginFailedException extends UserException {
    public LoginFailedException() {
        super(UserErrorCode.LOGIN_FAILED, Map.of());
    }
}
