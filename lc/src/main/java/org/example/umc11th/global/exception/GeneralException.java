package org.example.umc11th.global.exception;

import lombok.Getter;
import org.example.umc11th.global.apiPayload.code.BaseErrorCode;

@Getter
public class GeneralException extends RuntimeException {

    private final BaseErrorCode code;

    public GeneralException(BaseErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }
}
