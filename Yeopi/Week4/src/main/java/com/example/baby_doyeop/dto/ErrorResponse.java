package com.example.baby_doyeop.dto;

import java.util.List;

// 모든 오류를 같은 모양으로 돌려줘서 클라이언트가 원인을 구분할 수 있게 합니다.
public record ErrorResponse(
        int status,
        String code,
        String message,
        List<FieldError> errors
) {
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, List.of());
    }
}
