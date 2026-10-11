package com.example.baby_doyeop.exception;

import com.example.baby_doyeop.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 검증 실패: 어떤 필드가 왜 잘못됐는지 함께 알려줍니다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "INVALID_REQUEST", "요청 값이 올바르지 않습니다.", errors);
        return ResponseEntity.badRequest().body(body);
    }

    // JSON 문법 오류나 categoryId에 숫자가 아닌 값이 들어온 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "MALFORMED_JSON", "요청 본문 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(), "CATEGORY_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(DuplicateBookTitleException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTitle(DuplicateBookTitleException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(
                HttpStatus.CONFLICT.value(), "DUPLICATE_BOOK_TITLE", e.getMessage()));
    }

    // 동시에 같은 제목이 저장되는 경우처럼, 사전 확인을 통과해도 DB UNIQUE 제약에 걸리면 409로 응답합니다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.of(
                HttpStatus.CONFLICT.value(), "DATA_CONFLICT", "DB 제약 조건에 맞지 않는 요청입니다."));
    }
}
