package org.example.umc11th.domain.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.umc11th.global.apiPayload.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 이메일이 없을 때와 비밀번호가 틀렸을 때 같은 코드를 써서 가입 여부를 드러내지 않는다
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH401_1", "이메일 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH401_2", "로그인이 필요한 요청입니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH401_3", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH401_4", "토큰이 만료되었습니다. 다시 로그인해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
