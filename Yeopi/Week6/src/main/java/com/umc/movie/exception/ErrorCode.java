package com.umc.movie.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 실패 응답의 상태 코드·code·message를 한 곳에서 관리합니다.
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON4001", "요청 값이 올바르지 않습니다."),
    MALFORMED_JSON(HttpStatus.BAD_REQUEST, "COMMON4002", "요청 본문 형식이 올바르지 않습니다."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "COMMON4091", "이미 존재하는 데이터입니다."),

    // 회원 — [Challenge] 이메일과 닉네임 중복을 서로 다른 code로 구분
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4041", "존재하지 않는 유저입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER4091", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEMBER4092", "이미 사용 중인 닉네임입니다."),

    // 평점
    RATING_NOT_FOUND(HttpStatus.NOT_FOUND, "RATING4041", "존재하지 않는 평점입니다."),
    RATING_FORBIDDEN(HttpStatus.FORBIDDEN, "RATING4031", "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다."),
    DUPLICATE_RATING(HttpStatus.CONFLICT, "RATING4091", "이미 이 영화에 평점을 남기셨습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
