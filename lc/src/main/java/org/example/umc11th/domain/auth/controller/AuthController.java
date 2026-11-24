package org.example.umc11th.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.auth.dto.LoginRequest;
import org.example.umc11th.domain.auth.dto.LoginResponse;
import org.example.umc11th.domain.auth.service.AuthService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.example.umc11th.global.config.SwaggerDocs.JSON;

@Tag(name = "인증", description = "로그인과 토큰 발급 API. 토큰 없이 호출할 수 있는 공개 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 로그인",
            description = "이메일과 비밀번호로 로그인하고 Access Token을 발급받습니다. "
                    + "발급받은 토큰은 위쪽 Authorize 버튼에 넣어두면 인증이 필요한 API에 자동으로 실립니다. "
                    + "없는 이메일과 틀린 비밀번호는 같은 응답이라 가입 여부가 드러나지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 (COMMON200). result.accessToken에 토큰이 담김"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 규칙 위반 (COMMON400)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject("""
                            {"isSuccess": false, "code": "COMMON400", "message": "잘못된 요청입니다.", "result": {"email": "이메일 형식이 올바르지 않습니다."}}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않음 (AUTH401_1)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject("""
                            {"isSuccess": false, "code": "AUTH401_1", "message": "이메일 또는 비밀번호가 올바르지 않습니다.", "result": null}""")))
    })
    @SecurityRequirements // 공개 API - Swagger 문서에서 자물쇠를 뺀다
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.onSuccess("로그인에 성공했습니다.", authService.login(request));
    }
}
