package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @RequestBody DTO 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiResponse.failure(ErrorCode.INVALID_INPUT.getCode(), message));
    }

    // @PathVariable, @RequestParam 검증 실패 (5주차 중복 체크/페이징 파라미터)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodValidation(HandlerMethodValidationException exception) {
        String message = exception.getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure(ErrorCode.INVALID_INPUT.getCode(), message));
    }

    // 서비스에서 직접 던지는 업무 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
        return toResponse(exception.getErrorCode());
    }

    // 사전 중복 체크를 통과했지만 DB UNIQUE 제약에서 막힌 경우 (동시 요청 등)
    // 어떤 제약이 위반됐는지 보고 사전 체크와 같은 code/message로 바꿔 줍니다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException exception) {
        return toResponse(resolveConstraint(exception));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        return ResponseEntity.status(status)
                .body(ApiResponse.failure("COMMON" + status, exception.getReason()));
    }

    private ErrorCode resolveConstraint(DataIntegrityViolationException exception) {
        // MySQL 메시지 예: Duplicate entry 'umc@example.com' for key 'member.email'
        String message = exception.getMostSpecificCause().getMessage();
        if (message == null) {
            return ErrorCode.DUPLICATE_DATA;
        }
        if (message.contains("member.email")) {
            return ErrorCode.DUPLICATE_EMAIL;
        }
        if (message.contains("member.nickname")) {
            return ErrorCode.DUPLICATE_NICKNAME;
        }
        if (message.contains("uk_rating_member_movie")) {
            return ErrorCode.DUPLICATE_RATING;
        }
        return ErrorCode.DUPLICATE_DATA;
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }
}
