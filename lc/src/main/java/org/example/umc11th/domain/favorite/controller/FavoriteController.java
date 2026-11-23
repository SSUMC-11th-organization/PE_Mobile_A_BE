package org.example.umc11th.domain.favorite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.favorite.dto.CreateFavoriteRequest;
import org.example.umc11th.domain.favorite.dto.FavoriteResponse;
import org.example.umc11th.domain.favorite.dto.FavoriteStatusResponse;
import org.example.umc11th.domain.favorite.service.FavoriteService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.example.umc11th.global.auth.LoginMember;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 누구의 즐겨찾기인지는 URL이나 바디가 아니라 토큰으로 알아낸다
@Tag(name = "즐겨찾기", description = "로그인한 회원의 즐겨찾기. 모든 API에 Authorization: Bearer {accessToken} 헤더가 필요합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private static final String JSON = "application/json";
    private static final String DESC_401 = "토큰 없음·위조·만료 (AUTH401_2 / AUTH401_3 / AUTH401_4)";
    private static final String EX_400 = """
            {"isSuccess": false, "code": "COMMON400", "message": "잘못된 요청입니다.", "result": {"movieId": "영화 ID는 양수여야 합니다."}}""";
    private static final String EX_401 = """
            {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}""";
    private static final String EX_404 = """
            {"isSuccess": false, "code": "FAVORITE404_1", "message": "즐겨찾기한 영화를 찾을 수 없습니다.", "result": null}""";
    private static final String EX_409 = """
            {"isSuccess": false, "code": "FAVORITE409_1", "message": "이미 이 영화를 즐겨찾기하셨습니다.", "result": null}""";

    private final FavoriteService favoriteService;

    @Operation(summary = "즐겨찾기 등록", description = "토큰의 회원이 TMDB 영화 ID를 즐겨찾기에 추가합니다. 요청에 memberId는 받지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공 (COMMON201)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "movieId가 없거나 양수가 아님 (COMMON400)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_400))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DESC_401,
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 즐겨찾기한 영화 (FAVORITE409_1)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_409)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FavoriteResponse> create(
            @LoginMember Long memberId,
            @RequestBody @Valid CreateFavoriteRequest request
    ) {
        return ApiResponse.onCreated("즐겨찾기를 등록했습니다.", favoriteService.create(memberId, request));
    }

    @Operation(summary = "내 즐겨찾기 조회", description = "토큰의 회원이 등록한 즐겨찾기를 최신 등록순으로 돌려줍니다. 없으면 빈 배열입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (COMMON200)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DESC_401,
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401)))
    })
    @GetMapping
    public ApiResponse<List<FavoriteResponse>> getMyFavorites(@LoginMember Long memberId) {
        return ApiResponse.onSuccess("내 즐겨찾기 목록을 조회했습니다.", favoriteService.getMyFavorites(memberId));
    }

    @Operation(summary = "특정 영화 즐겨찾기 여부 조회", description = "즐겨찾기하지 않은 영화도 오류가 아니라 200과 isFavorite: false로 응답합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (COMMON200)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DESC_401,
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401)))
    })
    @GetMapping("/{movieId}")
    public ApiResponse<FavoriteStatusResponse> getStatus(
            @LoginMember Long memberId,
            @Parameter(description = "TMDB 영화 ID", example = "550") @PathVariable Long movieId
    ) {
        return ApiResponse.onSuccess("즐겨찾기 여부를 조회했습니다.", favoriteService.getStatus(memberId, movieId));
    }

    // 공통 응답 형식을 담기 위해 204 대신 200과 result: null로 응답한다
    @Operation(summary = "즐겨찾기 해제", description = "토큰의 회원이 즐겨찾기한 영화를 해제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "해제 성공 (COMMON200, result: null)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DESC_401,
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "즐겨찾기하지 않은 영화 (FAVORITE404_1)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_404)))
    })
    @DeleteMapping("/{movieId}")
    public ApiResponse<Void> delete(
            @LoginMember Long memberId,
            @Parameter(description = "TMDB 영화 ID", example = "550") @PathVariable Long movieId
    ) {
        favoriteService.delete(memberId, movieId);
        return ApiResponse.onSuccess("즐겨찾기를 해제했습니다.", null);
    }
}
