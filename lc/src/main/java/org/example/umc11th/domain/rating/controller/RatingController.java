package org.example.umc11th.domain.rating.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.rating.dto.CreateRatingRequest;
import org.example.umc11th.domain.rating.dto.RatingResponse;
import org.example.umc11th.domain.rating.dto.UpdateRatingRequest;
import org.example.umc11th.domain.rating.service.RatingService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 파일은 자원(Rating) 기준으로 나누고, URL은 프론트가 이해하기 쉬운 /members/... 모양으로 맞춤
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class RatingController {

    private final RatingService ratingService;

    @GetMapping("/{memberId}/ratings")
    public ApiResponse<List<RatingResponse>> getMemberRatings(@PathVariable Long memberId) {
        return ApiResponse.onSuccess(ratingService.getRatingsByMember(memberId));
    }

    @PostMapping("/{memberId}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> create(
            @PathVariable Long memberId,
            @RequestBody @Valid CreateRatingRequest request
    ) {
        return ApiResponse.onCreated("평점 등록에 성공했습니다.", ratingService.create(memberId, request));
    }

    @PatchMapping("/{memberId}/ratings/{ratingId}")
    public ApiResponse<Void> update(
            @PathVariable Long memberId,
            @PathVariable Long ratingId,
            @RequestBody @Valid UpdateRatingRequest request
    ) {
        ratingService.update(memberId, ratingId, request);
        return ApiResponse.onSuccess("평점 수정에 성공했습니다.", null);
    }

    @DeleteMapping("/{memberId}/ratings/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long memberId, @PathVariable Long ratingId) {
        ratingService.delete(memberId, ratingId);
    }
}
