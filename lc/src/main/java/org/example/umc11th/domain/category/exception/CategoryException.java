package org.example.umc11th.domain.category.exception;

import org.example.umc11th.global.exception.GeneralException;

public class CategoryException extends GeneralException {

    public CategoryException(CategoryErrorCode code) {
        super(code);
    }
}
