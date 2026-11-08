package org.example.umc11th.domain.rating.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.rating.dto.CreateRatingRequest;
import org.example.umc11th.domain.rating.dto.RatingResponse;
import org.example.umc11th.domain.rating.dto.UpdateRatingRequest;
import org.example.umc11th.domain.rating.service.RatingService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.example.umc11th.global.auth.LoginMember;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 평점 쓰기는 /ratings 아래로 옮기고, 누구의 요청인지는 URL이 아니라 토큰으로 알아낸다
@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // 공개 조회라 토큰을 받지 않는다 (인터셉터 예외 경로)
    @GetMapping("/members/{memberId}/ratings")
    public ApiResponse<List<RatingResponse>> getMemberRatings(@PathVariable Long memberId) {
        return ApiResponse.onSuccess(ratingService.getRatingsByMember(memberId));
    }

    @PostMapping("/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RatingResponse> create(
            @LoginMember Long memberId,
            @RequestBody @Valid CreateRatingRequest request
    ) {
        return ApiResponse.onCreated("평점을 등록했습니다.", ratingService.create(memberId, request));
    }

    @PatchMapping("/ratings/{ratingId}")
    public ApiResponse<RatingResponse> update(
            @LoginMember Long memberId,
            @PathVariable Long ratingId,
            @RequestBody @Valid UpdateRatingRequest request
    ) {
        return ApiResponse.onSuccess("평점을 수정했습니다.", ratingService.update(memberId, ratingId, request));
    }

    // 공통 응답 형식을 담기 위해 204 대신 200과 result: null로 응답한다
    @DeleteMapping("/ratings/{ratingId}")
    public ApiResponse<Void> delete(
            @LoginMember Long memberId,
            @PathVariable Long ratingId
    ) {
        ratingService.delete(memberId, ratingId);
        return ApiResponse.onSuccess("평점을 삭제했습니다.", null);
    }
}
