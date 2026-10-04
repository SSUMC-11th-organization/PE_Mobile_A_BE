package org.example.umc11th.domain.book.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.umc11th.global.apiPayload.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BookErrorCode implements BaseErrorCode {

    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK404", "존재하지 않는 도서입니다."),
    BOOK_ALREADY_RENTED(HttpStatus.CONFLICT, "BOOK409", "이미 대여 중인 도서입니다."),
    BOOK_TITLE_DUPLICATED(HttpStatus.CONFLICT, "BOOK409_2", "이미 등록된 도서 제목입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
