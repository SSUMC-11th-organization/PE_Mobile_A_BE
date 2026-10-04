package org.example.umc11th.domain.rental.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.umc11th.global.apiPayload.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RentalErrorCode implements BaseErrorCode {

    RENTAL_NOT_FOUND(HttpStatus.NOT_FOUND, "RENTAL404", "존재하지 않는 대여 기록입니다."),
    RENTAL_ALREADY_RETURNED(HttpStatus.CONFLICT, "RENTAL409", "이미 반납된 대여 기록입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
