package com.umc.movie.exception;

import com.umc.movie.dto.ApiResponse;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Locale;

// 어느 Controller에서 실패해도 같은 ApiResponse 형식으로 응답합니다.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1번 문: DTO 검증(@Valid) 실패 → 400, 첫 번째 필드의 메시지를 그대로 전달
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return respond(ErrorCode.INVALID_INPUT, message);
    }

    // @PathVariable·@RequestParam 검증(5주차 닉네임 2~12자, 이메일 형식, page·size) 실패 → 400
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleParameterValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return respond(ErrorCode.INVALID_INPUT, message);
    }

    // /members/abc/ratings처럼 숫자 자리에 문자가 온 경우 → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return respond(ErrorCode.INVALID_INPUT, e.getName() + " 값의 형식이 올바르지 않습니다.");
    }

    // JSON 문법 오류, score에 "다섯" 같은 문자가 온 경우 → 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e) {
        return respond(ErrorCode.MALFORMED_JSON, ErrorCode.MALFORMED_JSON.getMessage());
    }

    // 2번 문: 서비스 검증 실패 → ErrorCode에 정한 404/403/409
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return respond(e.getErrorCode(), e.getErrorCode().getMessage());
    }

    // 3번 문: 사전 중복 체크를 통과했지만 동시에 저장되어 DB UNIQUE 제약에 걸린 경우 → 409
    // [Challenge] 어떤 제약에 걸렸는지 보고 서비스 검증과 같은 code로 맞춥니다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        ErrorCode errorCode = resolveConstraint(e);
        return respond(errorCode, errorCode.getMessage());
    }

    private ErrorCode resolveConstraint(DataIntegrityViolationException e) {
        Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
        String detail = String.valueOf(cause.getMessage()).toLowerCase(Locale.ROOT);
        if (detail.contains("uk_rating_member_movie")) {
            return ErrorCode.DUPLICATE_RATING;
        }
        if (detail.contains("email")) {
            return ErrorCode.DUPLICATE_EMAIL;
        }
        if (detail.contains("nickname")) {
            return ErrorCode.DUPLICATE_NICKNAME;
        }
        return ErrorCode.DATA_CONFLICT;
    }

    private ResponseEntity<ApiResponse<Void>> respond(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), message));
    }
}
