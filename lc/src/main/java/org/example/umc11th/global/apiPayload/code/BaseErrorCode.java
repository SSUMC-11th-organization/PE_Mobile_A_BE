package org.example.umc11th.global.apiPayload.code;

import org.springframework.http.HttpStatus;

// 모든 도메인의 에러 코드 enum이 구현하는 공통 규격
public interface BaseErrorCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
