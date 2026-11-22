package com.umc.movie.controller;

import com.umc.movie.auth.LoginMember;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
import com.umc.movie.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 4개 모두 토큰이 필요합니다. WebConfig의 공개 경로 목록에 /favorites를 넣지 않았으므로 인터셉터가 자동으로 막아 줍니다.
// (Swagger의 @ApiResponse는 우리 dto.ApiResponse와 이름이 같아서 전체 이름으로 씁니다.)
@Tag(name = "Favorite", description = "즐겨찾기 (Authorization: Bearer {token} 필요)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private static final String UNAUTHORIZED = """
            {"isSuccess":false,"code":"AUTH401_2","message":"로그인이 필요한 요청입니다.","result":null}""";

    private final FavoriteService favoriteService;

    @Operation(summary = "즐겨찾기 등록", description = "토큰의 회원이 movieId(TMDB 영화 ID)를 즐겨찾기합니다. 요청에 memberId는 없습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "movieId 누락 또는 0 이하 (COMMON4001)",
                    content = @Content(examples = @ExampleObject("""
                            {"isSuccess":false,"code":"COMMON4001","message":"영화 ID는 양수여야 합니다.","result":null}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음/위조/만료 (AUTH401_2~4)",
                    content = @Content(examples = @ExampleObject(UNAUTHORIZED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 즐겨찾기한 영화 (FAVORITE409_1)",
                    content = @Content(examples = @ExampleObject("""
                            {"isSuccess":false,"code":"FAVORITE409_1","message":"이미 이 영화를 즐겨찾기하셨습니다.","result":null}""")))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> create(
            @LoginMember Long memberId,
            @Valid @RequestBody CreateFavoriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("즐겨찾기를 등록했습니다.", favoriteService.create(memberId, request)));
    }

    @Operation(summary = "내 즐겨찾기 목록", description = "토큰의 회원 것만 최신 등록 순으로 반환합니다. 없으면 200과 빈 배열입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (빈 목록 포함)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음/위조/만료 (AUTH401_2~4)",
                    content = @Content(examples = @ExampleObject(UNAUTHORIZED)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getMyFavorites(@LoginMember Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok("내 즐겨찾기 목록을 조회했습니다.", favoriteService.getMyFavorites(memberId)));
    }

    @Operation(summary = "특정 영화 즐겨찾기 여부", description = "즐겨찾기하지 않았어도 오류가 아니라 200과 isFavorite=false입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(examples = @ExampleObject("""
                            {"isSuccess":true,"code":"COMMON200","message":"즐겨찾기 여부를 조회했습니다.","result":{"isFavorite":true}}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음/위조/만료 (AUTH401_2~4)",
                    content = @Content(examples = @ExampleObject(UNAUTHORIZED)))
    })
    @GetMapping("/{movieId}")
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> getStatus(
            @LoginMember Long memberId,
            @PathVariable @Positive(message = "영화 ID는 양수여야 합니다.") Long movieId) {
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기 여부를 조회했습니다.", favoriteService.getStatus(memberId, movieId)));
    }

    @Operation(summary = "즐겨찾기 해제", description = "공통 응답 형식을 지키기 위해 204가 아니라 200과 result=null을 돌려줍니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "해제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음/위조/만료 (AUTH401_2~4)",
                    content = @Content(examples = @ExampleObject(UNAUTHORIZED))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "내 즐겨찾기에 없는 영화 (FAVORITE404_1)",
                    content = @Content(examples = @ExampleObject("""
                            {"isSuccess":false,"code":"FAVORITE404_1","message":"즐겨찾기한 영화를 찾을 수 없습니다.","result":null}""")))
    })
    @DeleteMapping("/{movieId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @LoginMember Long memberId,
            @PathVariable @Positive(message = "영화 ID는 양수여야 합니다.") Long movieId) {
        favoriteService.delete(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기를 해제했습니다.", null));
    }
}
