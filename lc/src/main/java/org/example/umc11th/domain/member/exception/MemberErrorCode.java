package org.example.umc11th.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.umc11th.global.apiPayload.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_1", "해당 회원을 찾을 수 없습니다."),
    MEMBER_NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "MEMBER409_1", "이미 사용 중인 닉네임입니다."),
    MEMBER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "MEMBER409_2", "이미 사용 중인 이메일입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
