package com.umc.movie.controller;

import com.umc.movie.config.OpenApiConfig;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingPageResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 유저별 평점 조회는 공개 API라서 @SecurityRequirement를 클래스가 아닌 메서드마다 붙입니다
@Tag(name = "평점", description = "평점 조회/등록/수정/삭제 API")
@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @Operation(summary = "유저별 평점 조회",
            description = "특정 회원이 남긴 평점을 최신순으로 페이지 단위로 조회합니다. 토큰 없이 호출할 수 있는 공개 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {"content": [{"ratingId": 1, "movieId": 550, "score": 5, "comment": "인생 영화입니다.",
                                          "createdAt": "2026-09-13T21:00:00", "updatedAt": "2026-09-13T21:00:00"}],
                             "page": 0, "size": 10, "totalElements": 1, "totalPages": 1, "hasNext": false}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "page/size 범위 오류",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "COMMON4001", "message": "size는 100 이하여야 합니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 회원",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "MEMBER404_1", "message": "해당 회원을 찾을 수 없습니다.", "result": null}""")))
    })
    @GetMapping("/members/{memberId}/ratings")
    public RatingPageResponse getMemberRatings(
            @Parameter(description = "평점을 조회할 회원 ID", example = "1") @PathVariable Long memberId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @Parameter(description = "페이지 크기 (1~100)", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size) {
        return ratingService.getRatingsByMember(memberId, page, size);
    }

    @Operation(summary = "평점 등록", description = "토큰의 주인인 회원이 영화 하나에 평점을 남깁니다. 같은 영화에는 한 번만 남길 수 있습니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": true, "code": "COMMON201", "message": "평점을 등록했습니다.",
                             "result": {"ratingId": 1, "movieId": 550, "score": 5, "comment": "인생 영화입니다.",
                                        "createdAt": "2026-09-13T21:00:00", "updatedAt": "2026-09-13T21:00:00"}}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "COMMON4001", "message": "별점은 5점 이하여야 합니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 평점을 남긴 영화",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "RATING409_1", "message": "이미 이 영화에 평점을 남기셨습니다.", "result": null}""")))
    })
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid CreateRatingRequest request
    ) {
        RatingResponse response = ratingService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("평점을 등록했습니다.", response));
    }

    @Operation(summary = "평점 수정", description = "내가 남긴 평점의 별점과 후기를 수정합니다. 다른 회원의 평점은 수정할 수 없습니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": true, "code": "COMMON200", "message": "평점을 수정했습니다.",
                             "result": {"ratingId": 1, "movieId": 550, "score": 4, "comment": "다시 보니 조금 아쉬워요.",
                                        "createdAt": "2026-09-13T21:00:00", "updatedAt": "2026-09-14T10:00:00"}}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "COMMON4001", "message": "별점을 입력해주세요.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 회원의 평점",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "RATING403_1", "message": "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 평점",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "RATING404_1", "message": "해당 평점을 찾을 수 없습니다.", "result": null}""")))
    })
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "수정할 평점 ID", example = "1") @PathVariable Long ratingId,
            @RequestBody @Valid UpdateRatingRequest request
    ) {
        RatingResponse response = ratingService.update(memberId, ratingId, request);
        return ResponseEntity.ok(ApiResponse.ok("평점을 수정했습니다.", response));
    }

    @Operation(summary = "평점 삭제", description = "내가 남긴 평점을 삭제합니다. 다른 회원의 평점은 삭제할 수 없습니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": true, "code": "COMMON200", "message": "평점을 삭제했습니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 회원의 평점",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "RATING403_1", "message": "본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 평점",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "RATING404_1", "message": "해당 평점을 찾을 수 없습니다.", "result": null}""")))
    })
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "삭제할 평점 ID", example = "1") @PathVariable Long ratingId
    ) {
        ratingService.delete(memberId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok("평점을 삭제했습니다.", null));
    }
}
