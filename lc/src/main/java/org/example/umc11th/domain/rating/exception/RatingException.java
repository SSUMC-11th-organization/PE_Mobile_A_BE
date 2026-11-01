package org.example.umc11th.domain.rating.exception;

import org.example.umc11th.global.exception.GeneralException;

public class RatingException extends GeneralException {

    public RatingException(RatingErrorCode code) {
        super(code);
    }
}
