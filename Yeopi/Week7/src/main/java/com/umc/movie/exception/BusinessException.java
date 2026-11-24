package com.umc.movie.exception;

import lombok.Getter;

// 서비스 검증(존재 여부·중복·소유자)에서 실패했을 때 던지는 예외입니다.
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
