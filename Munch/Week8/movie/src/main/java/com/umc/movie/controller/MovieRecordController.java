package com.umc.movie.controller;

import com.umc.movie.config.OpenApiConfig;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.MovieRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MovieRecord", description = "평점 + 즐겨찾기 동시 저장 API (로그인 필요)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequiredArgsConstructor
public class MovieRecordController {

    private final MovieRecordService movieRecordService;

    @Operation(summary = "영화 기록 저장",
            description = "평점을 저장하고, addToFavorites가 true면 즐겨찾기도 같은 트랜잭션에서 저장합니다. "
                    + "하나라도 실패하면 모두 rollback 됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "저장 성공",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": true, "code": "COMMON201", "message": "영화 기록을 저장했습니다.",
                             "result": {
                               "rating": {"ratingId": 3, "movieId": 238, "score": 5, "comment": "다시 보고 싶은 영화입니다.",
                                          "createdAt": "2026-09-06T21:10:00", "updatedAt": "2026-09-06T21:10:00"},
                               "favorite": {"favoriteId": 2, "movieId": 238, "createdAt": "2026-09-06T21:10:00"}}}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "COMMON4001", "message": "별점은 5점 이하여야 합니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 평점을 남겼거나 이미 즐겨찾기한 영화",
                    content = @Content(examples = {
                            @ExampleObject(name = "RATING409_1", value = """
                                    {"isSuccess": false, "code": "RATING409_1", "message": "이미 이 영화에 평점을 남기셨습니다.", "result": null}"""),
                            @ExampleObject(name = "FAVORITE409_1", value = """
                                    {"isSuccess": false, "code": "FAVORITE409_1", "message": "이미 이 영화를 즐겨찾기하셨습니다.", "result": null}""")
                    }))
    })
    @PostMapping("/movie-records")
    public ResponseEntity<ApiResponse<MovieRecordResponse>> create(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid CreateMovieRecordRequest request
    ) {
        MovieRecordResponse response = movieRecordService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("영화 기록을 저장했습니다.", response));
    }
}
