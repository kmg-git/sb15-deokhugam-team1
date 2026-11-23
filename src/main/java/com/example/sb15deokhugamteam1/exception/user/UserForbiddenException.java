package com.example.sb15deokhugamteam1.exception.user;

import java.util.Map;
import java.util.UUID;

public class UserForbiddenException extends UserException {


    public UserForbiddenException(UUID userId) {

        super(UserErrorCode.USER_FORBIDDEN, Map.of("userId",userId));
    }


}