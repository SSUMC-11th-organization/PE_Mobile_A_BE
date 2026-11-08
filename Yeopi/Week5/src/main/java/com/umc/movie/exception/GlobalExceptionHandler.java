package com.umc.movie.exception;

import com.umc.movie.dto.ErrorResponse;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @PathVariable·@RequestParam에 붙인 검증(@Size, @Email, @Min) 실패 → 400
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("요청 값이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
    }

    // /members/abc/ratings처럼 숫자 자리에 문자가 온 경우 → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, e.getName() + " 값의 형식이 올바르지 않습니다."));
    }

    // 존재하지 않는 유저 → 404 (Service의 ResponseStatusException)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException e) {
        int status = e.getStatusCode().value();
        return ResponseEntity.status(status).body(new ErrorResponse(status, e.getReason()));
    }
}
