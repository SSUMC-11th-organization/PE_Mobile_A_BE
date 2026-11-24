package org.example.umc11th.domain.rating.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static org.example.umc11th.global.config.SwaggerDocs.DESC_401;
import static org.example.umc11th.global.config.SwaggerDocs.EX_401;
import static org.example.umc11th.global.config.SwaggerDocs.EX_MEMBER_404;
import static org.example.umc11th.global.config.SwaggerDocs.JSON;

// 평점 쓰기는 /ratings 아래로 옮기고, 누구의 요청인지는 URL이 아니라 토큰으로 알아낸다
@Tag(name = "평점", description = "영화 평점 API. 유저별 조회만 공개이고, 등록·수정·삭제는 Authorization: Bearer {accessToken} 헤더가 필요합니다.")
@RestController
@RequiredArgsConstructor
public class RatingController {

    private static final String EX_403 = """
            {"isSuccess": false, "code": "RATING403_1", "message": "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.", "result": null}""";
    private static final String EX_404 = """
            {"isSuccess": false, "code": "RATING404_1", "message": "해당 평점을 찾을 수 없습니다.", "result": null}""";

    private final RatingService ratingService;

    // 공개 조회라 토큰을 받지 않는다 (인터셉터 예외 경로)
    @Operation(summary = "유저별 평점 조회", description = "회원 한 명이 남긴 평점을 최신순으로 돌려줍니다. 토큰 없이 호출할 수 있는 공개 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (COMMON200). 평점이 없으면 빈 배열"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "없는 회원 (MEMBER404_1)", content = @Content(mediaType = JSON, examples = @ExampleObject(EX_MEMBER_404)))
    })
    @SecurityRequirements // 공개 API - Swagger 문서에서 자물쇠를 뺀다
    @GetMapping("/members/{memberId}/ratings")
    public ApiResponse<List<RatingResponse>> getMemberRatings(
            @Parameter(description = "평점을 조회할 회원 ID", example = "1") @PathVariable Long memberId) {
        return ApiResponse.onSuccess(ratingService.getRatingsByMember(memberId));
    }

    @Operation(summary = "평점 등록", description = "토큰의 주인인 회원이 영화 한 편에 평점을 남깁니다. 요청에 memberId는 받지 않습니다. 같은 영화에는 한 번만 남길 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공 (COMMON201)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 규칙 위반 (COMMON400)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject("""
                            {"isSuccess": false, "code": "COMMON400", "message": "잘못된 요청입니다.", "result": {"score": "별점은 5점 이하여야 합니다."}}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DESC_401,
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "토큰의 회원이 이미 없음 (MEMBER404_1)", content = @Content(mediaType = JSON, examples = @ExampleObject(EX_MEMBER_404))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 이 영화에 평점을 남김 (RATING409_1)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject("""
                            {"isSuccess": false, "code": "RATING409_1", "message": "이미 이 영화에 평점을 남기셨습니다.", "result": null}""")))
    })
    @PostMapping("/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RatingResponse> create(
            @LoginMember Long memberId,
            @RequestBody @Valid CreateRatingRequest request
    ) {
        return ApiResponse.onCreated("평점을 등록했습니다.", ratingService.create(memberId, request));
    }

    @Operation(summary = "평점 수정", description = "내가 남긴 평점의 별점과 후기를 고칩니다. 남의 평점은 고칠 수 없습니다(403). 토큰이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공 (COMMON200)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 규칙 위반 (COMMON400)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject("""
                            {"isSuccess": false, "code": "COMMON400", "message": "잘못된 요청입니다.", "result": {"score": "별점은 1점 이상이어야 합니다."}}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DESC_401,
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "남의 평점 (RATING403_1)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_403))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "없는 평점 (RATING404_1)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_404)))
    })
    @PatchMapping("/ratings/{ratingId}")
    public ApiResponse<RatingResponse> update(
            @LoginMember Long memberId,
            @Parameter(description = "수정할 평점 ID", example = "1") @PathVariable Long ratingId,
            @RequestBody @Valid UpdateRatingRequest request
    ) {
        return ApiResponse.onSuccess("평점을 수정했습니다.", ratingService.update(memberId, ratingId, request));
    }

    // 공통 응답 형식을 담기 위해 204 대신 200과 result: null로 응답한다
    @Operation(summary = "평점 삭제", description = "내가 남긴 평점을 지웁니다. 남의 평점은 지울 수 없습니다(403). 토큰이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공 (COMMON200, result: null)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DESC_401,
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "남의 평점 (RATING403_1)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_403))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "없는 평점 (RATING404_1)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_404)))
    })
    @DeleteMapping("/ratings/{ratingId}")
    public ApiResponse<Void> delete(
            @LoginMember Long memberId,
            @Parameter(description = "삭제할 평점 ID", example = "1") @PathVariable Long ratingId
    ) {
        ratingService.delete(memberId, ratingId);
        return ApiResponse.onSuccess("평점을 삭제했습니다.", null);
    }
}
