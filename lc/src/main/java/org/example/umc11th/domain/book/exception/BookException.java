package org.example.umc11th.domain.book.exception;

import org.example.umc11th.global.exception.GeneralException;

public class BookException extends GeneralException {

    public BookException(BookErrorCode code) {
        super(code);
    }
}
