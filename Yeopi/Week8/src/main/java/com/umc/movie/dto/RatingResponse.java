package com.umc.movie.dto;

import com.umc.movie.entity.Rating;

import java.time.LocalDateTime;

// API 명세에 합의한 응답 모양. member 정보는 Path에 이미 있으므로 응답에 넣지 않습니다.
public record RatingResponse(
        Long ratingId,
        Long movieId,
        Integer score,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RatingResponse from(Rating rating) {
        return new RatingResponse(
                rating.getRatingId(),
                rating.getMovieId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}
