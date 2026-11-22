package com.umc.movie.controller;

import com.umc.movie.auth.LoginMember;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.service.RatingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 7주차: 쓰기 API는 경로의 memberId 대신 토큰의 memberId(@LoginMember)로 "누가" 요청했는지 판단합니다.
// 경로에 memberId가 있으면 남의 번호를 넣어 그 사람인 척할 수 있기 때문입니다.
@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // GET /members/{memberId}/ratings  (공개, ?page=0&size=10)
    // 남의 평점 목록은 누구나 볼 수 있으므로 경로의 memberId를 그대로 씁니다.
    @SecurityRequirements() // 공개 API (토큰 불필요)
    @GetMapping("/members/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(
            @PathVariable Long memberId,
            @RequestParam(required = false) @Min(value = 0, message = "page는 0 이상이어야 합니다.") Integer page,
            @RequestParam(required = false) @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") Integer size) {
        return ratingService.getRatingsByMember(memberId, page, size);
    }

    // POST /ratings → 201 (토큰 필요)
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @LoginMember Long memberId,
            @Valid @RequestBody CreateRatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("평점 등록에 성공했습니다.", ratingService.create(memberId, request)));
    }

    // PATCH /ratings/{ratingId} → 200 (토큰 필요, 내 평점만)
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @LoginMember Long memberId,
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("평점 수정에 성공했습니다.", ratingService.update(memberId, ratingId, request)));
    }

    // DELETE /ratings/{ratingId} → 200 (토큰 필요, 내 평점만)
    // 6주차는 204였지만, 이번 주는 다른 API와 같은 ApiResponse 형식으로 결과 메시지를 돌려줍니다.
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(@LoginMember Long memberId, @PathVariable Long ratingId) {
        ratingService.delete(memberId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok("평점 삭제에 성공했습니다.", null));
    }
}
