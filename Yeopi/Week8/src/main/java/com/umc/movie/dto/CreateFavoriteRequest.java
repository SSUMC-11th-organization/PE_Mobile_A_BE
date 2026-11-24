package com.umc.movie.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// POST /favorites 요청 본문. 누구의 즐겨찾기인지는 토큰이 알려주므로 memberId는 없습니다.
public record CreateFavoriteRequest(
        @NotNull(message = "영화 ID는 필수입니다.")
        @Positive(message = "영화 ID는 양수여야 합니다.")
        Long movieId
) {
}
