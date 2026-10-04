package org.example.umc11th.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, "COMMON409", "이미 존재하거나 제약 조건에 위배되는 데이터입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
