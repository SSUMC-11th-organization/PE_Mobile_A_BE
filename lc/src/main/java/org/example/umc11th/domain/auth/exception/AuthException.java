package org.example.umc11th.domain.auth.exception;

import org.example.umc11th.global.exception.GeneralException;

public class AuthException extends GeneralException {

    public AuthException(AuthErrorCode code) {
        super(code);
    }
}
