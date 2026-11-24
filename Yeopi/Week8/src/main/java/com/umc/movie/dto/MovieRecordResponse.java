package com.umc.movie.dto;

// addToFavorites가 false면 favorite은 null입니다.
public record MovieRecordResponse(
        RatingResponse rating,
        FavoriteResponse favorite
) {
}
