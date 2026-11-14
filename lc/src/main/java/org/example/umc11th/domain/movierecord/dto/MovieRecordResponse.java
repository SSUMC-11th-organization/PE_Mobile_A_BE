package org.example.umc11th.domain.movierecord.dto;

import org.example.umc11th.domain.favorite.dto.FavoriteResponse;
import org.example.umc11th.domain.rating.dto.RatingResponse;

// addToFavorites가 false면 favorite은 null
public record MovieRecordResponse(
        RatingResponse rating,
        FavoriteResponse favorite
) {
}
