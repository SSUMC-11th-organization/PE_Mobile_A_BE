package org.example.umc11th.domain.favorite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FavoriteStatusResponse(
        // boolean의 is 접두어가 JSON에서 빠지지 않도록 이름을 고정한다
        @JsonProperty("isFavorite")
        boolean isFavorite
) {
}
