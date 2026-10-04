package org.example.umc11th.domain.user.exception;

import org.example.umc11th.global.exception.GeneralException;

public class UserException extends GeneralException {

    public UserException(UserErrorCode code) {
        super(code);
    }
}
