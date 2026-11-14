package org.example.umc11th.domain.favorite.exception;

import org.example.umc11th.global.exception.GeneralException;

public class FavoriteException extends GeneralException {

    public FavoriteException(FavoriteErrorCode code) {
        super(code);
    }
}
