package com.umc.movie.dto;

public record MovieRecordResponse(
        RatingResponse rating,
        FavoriteResponse favorite // addToFavorites가 false면 null
) {
}
