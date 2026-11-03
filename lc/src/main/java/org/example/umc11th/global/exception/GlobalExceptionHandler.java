package org.example.umc11th.global.exception;

import org.example.umc11th.domain.member.exception.MemberErrorCode;
import org.example.umc11th.domain.rating.exception.RatingErrorCode;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.example.umc11th.global.apiPayload.code.BaseErrorCode;
import org.example.umc11th.global.apiPayload.code.GeneralErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    // DB 제약 조건 위반 (409) - 동시 요청은 서비스의 사전 중복 체크를 함께 통과할 수 있어 DB UNIQUE가 최종적으로 막는다
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        BaseErrorCode code = resolveConflictCode(e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code.getCode(), code.getMessage(), null));
    }

    // MySQL 메시지의 제약 이름을 보고, 사전 중복 체크에 걸렸을 때와 같은 코드로 응답한다
    private BaseErrorCode resolveConflictCode(String message) {
        if (message == null) {
            return GeneralErrorCode.CONFLICT;
        }
        if (message.contains("member.email")) {
            return MemberErrorCode.MEMBER_EMAIL_DUPLICATED;
        }
        if (message.contains("member.nickname")) {
            return MemberErrorCode.MEMBER_NICKNAME_DUPLICATED;
        }
        if (message.contains("uk_rating_member_movie")) {
            return RatingErrorCode.RATING_DUPLICATED;
        }
        return GeneralErrorCode.CONFLICT;
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

    // JSON 형식 오류, 숫자 자리에 문자가 온 경우 등 (400) - 기본 오류 응답 대신 같은 형식으로 응답
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        BaseErrorCode code = GeneralErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code.getCode(), code.getMessage(), null));
    }
}
