package org.example.umc11th.domain.movierecord.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.movierecord.dto.CreateMovieRecordRequest;
import org.example.umc11th.domain.movierecord.dto.MovieRecordResponse;
import org.example.umc11th.domain.movierecord.service.MovieRecordService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.example.umc11th.global.auth.LoginMember;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "영화 기록", description = "평점과 즐겨찾기를 한 번에 저장. Authorization: Bearer {accessToken} 헤더가 필요합니다.")
@RestController
@RequiredArgsConstructor
public class MovieRecordController {

    private static final String JSON = "application/json";
    private static final String EX_400 = """
            {"isSuccess": false, "code": "COMMON400", "message": "잘못된 요청입니다.", "result": {"score": "별점은 5점 이하여야 합니다."}}""";
    private static final String EX_401 = """
            {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}""";
    private static final String EX_409_RATING = """
            {"isSuccess": false, "code": "RATING409_1", "message": "이미 이 영화에 평점을 남기셨습니다.", "result": null}""";
    private static final String EX_409_FAVORITE = """
            {"isSuccess": false, "code": "FAVORITE409_1", "message": "이미 이 영화를 즐겨찾기하셨습니다.", "result": null}""";

    private final MovieRecordService movieRecordService;

    @Operation(summary = "영화 기록 저장",
            description = "평점을 저장하고, addToFavorites가 true면 즐겨찾기도 같은 트랜잭션에서 저장합니다. 하나라도 실패하면 둘 다 저장되지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "저장 성공 (COMMON201). addToFavorites가 false면 favorite은 null"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 규칙 위반 (COMMON400)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_400))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음·위조·만료 (AUTH401_2 / AUTH401_3 / AUTH401_4)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 평점을 남김 (RATING409_1) / 이미 즐겨찾기함 (FAVORITE409_1)",
                    content = @Content(mediaType = JSON, examples = {
                            @ExampleObject(name = "RATING409_1", value = EX_409_RATING),
                            @ExampleObject(name = "FAVORITE409_1", value = EX_409_FAVORITE)
                    }))
    })
    @PostMapping("/movie-records")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MovieRecordResponse> create(
            @LoginMember Long memberId,
            @RequestBody @Valid CreateMovieRecordRequest request
    ) {
        return ApiResponse.onCreated("영화 기록을 저장했습니다.", movieRecordService.create(memberId, request));
    }
}
