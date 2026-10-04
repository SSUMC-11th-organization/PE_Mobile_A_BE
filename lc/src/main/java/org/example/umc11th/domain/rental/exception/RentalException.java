package org.example.umc11th.domain.rental.exception;

import org.example.umc11th.global.exception.GeneralException;

public class RentalException extends GeneralException {

    public RentalException(RentalErrorCode code) {
        super(code);
    }
}
