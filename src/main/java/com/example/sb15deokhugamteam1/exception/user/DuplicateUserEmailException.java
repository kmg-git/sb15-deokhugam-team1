package com.example.sb15deokhugamteam1.exception.user;

import java.util.Map;

public class DuplicateUserEmailException extends UserException {


    public DuplicateUserEmailException(String email) {

        super(UserErrorCode.DUPLICATE_USER_EMAIL, Map.of("email",email));
    }


}