package com.umc.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// GET /favorites/{movieId} 응답. 즐겨찾기하지 않은 것도 정상 상태라 404가 아니라 false로 알려줍니다.
// boolean 필드 이름이 is로 시작하면 Jackson이 "favorite"으로 바꿔버릴 수 있어서 이름을 고정합니다.
public record FavoriteStatusResponse(
        @JsonProperty("isFavorite") boolean isFavorite
) {
}
