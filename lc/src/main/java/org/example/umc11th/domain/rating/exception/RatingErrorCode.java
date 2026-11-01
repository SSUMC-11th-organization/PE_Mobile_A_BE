package org.example.umc11th.domain.rating.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.umc11th.global.apiPayload.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RatingErrorCode implements BaseErrorCode {

    RATING_FORBIDDEN(HttpStatus.FORBIDDEN, "RATING403", "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다."),
    RATING_NOT_FOUND(HttpStatus.NOT_FOUND, "RATING404", "존재하지 않는 평점입니다."),
    RATING_DUPLICATED(HttpStatus.CONFLICT, "RATING409", "이미 이 영화에 평점을 남기셨습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
