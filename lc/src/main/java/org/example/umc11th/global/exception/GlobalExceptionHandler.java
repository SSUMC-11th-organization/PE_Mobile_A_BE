package org.example.umc11th.global.exception;

import org.example.umc11th.global.apiPayload.ApiResponse;
import org.example.umc11th.global.apiPayload.code.BaseErrorCode;
import org.example.umc11th.global.apiPayload.code.GeneralErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 직접 던진 예외 (404, 409 등) - 도메인별 예외(BookException 등)도 모두 여기서 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(GeneralException e) {
        BaseErrorCode code = e.getCode();
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code.getCode(), code.getMessage(), null));
    }

    // DB 제약 조건 위반 (409) - 동시 요청으로 existsByTitle 검사를 통과한 중복 등
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        BaseErrorCode code = GeneralErrorCode.CONFLICT;
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code.getCode(), code.getMessage(), null));
    }

    // @Valid 검증 실패 (400) - 어떤 필드가 왜 틀렸는지 함께 응답
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code.getCode(), code.getMessage(), errors));
    }
}
