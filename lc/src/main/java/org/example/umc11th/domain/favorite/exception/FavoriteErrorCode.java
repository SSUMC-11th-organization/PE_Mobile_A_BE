package org.example.umc11th.domain.favorite.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.umc11th.global.apiPayload.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FavoriteErrorCode implements BaseErrorCode {

    FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "FAVORITE404_1", "즐겨찾기한 영화를 찾을 수 없습니다."),
    FAVORITE_DUPLICATED(HttpStatus.CONFLICT, "FAVORITE409_1", "이미 이 영화를 즐겨찾기하셨습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
