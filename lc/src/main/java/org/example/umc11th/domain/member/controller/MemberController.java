package org.example.umc11th.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.member.dto.EmailCheckResponse;
import org.example.umc11th.domain.member.dto.MemberInfoResponse;
import org.example.umc11th.domain.member.dto.MemberResponse;
import org.example.umc11th.domain.member.dto.NicknameCheckResponse;
import org.example.umc11th.domain.member.dto.MemberUpdateRequest;
import org.example.umc11th.domain.member.dto.SignUpRequest;
import org.example.umc11th.domain.member.service.MemberService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.example.umc11th.global.auth.LoginMember;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.example.umc11th.global.config.SwaggerDocs.DESC_401;
import static org.example.umc11th.global.config.SwaggerDocs.EX_401;
import static org.example.umc11th.global.config.SwaggerDocs.EX_MEMBER_404;
import static org.example.umc11th.global.config.SwaggerDocs.JSON;

@Tag(name = "회원", description = "회원가입, 중복 체크(공개)와 내 정보 조회·수정(토큰 필요) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "닉네임 중복 체크", description = "사용 가능한 닉네임인지 확인합니다. 토큰 없이 호출할 수 있는 공개 API입니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공 (COMMON200). 중복 여부는 result에 담김"))
    @SecurityRequirements // 공개 API - Swagger 문서에서 자물쇠를 뺀다
    @GetMapping("/nickname/{nickname}")
    public ApiResponse<NicknameCheckResponse> checkNickname(
            @Parameter(description = "확인할 닉네임", example = "영화조아") @PathVariable String nickname) {
        return ApiResponse.onSuccess(memberService.checkNickname(nickname));
    }

    @Operation(summary = "이메일 중복 체크", description = "사용 가능한 이메일인지 확인합니다. 토큰 없이 호출할 수 있는 공개 API입니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공 (COMMON200). 중복 여부는 result에 담김"))
    @SecurityRequirements // 공개 API - Swagger 문서에서 자물쇠를 뺀다
    @GetMapping("/email/{email}")
    public ApiResponse<EmailCheckResponse> checkEmail(
            @Parameter(description = "확인할 이메일", example = "challenger@umc.com") @PathVariable String email) {
        return ApiResponse.onSuccess(memberService.checkEmail(email));
    }

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 회원을 등록합니다. 토큰 없이 호출할 수 있는 공개 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "가입 성공 (COMMON201)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 규칙 위반 (COMMON400)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject("""
                            {"isSuccess": false, "code": "COMMON400", "message": "잘못된 요청입니다.", "result": {"nickname": "닉네임은 2자 이상 12자 이하로 입력해주세요."}}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임 (MEMBER409_1) / 이미 사용 중인 이메일 (MEMBER409_2)",
                    content = @Content(mediaType = JSON, examples = {
                            @ExampleObject(name = "MEMBER409_1", value = """
                                    {"isSuccess": false, "code": "MEMBER409_1", "message": "이미 사용 중인 닉네임입니다.", "result": null}"""),
                            @ExampleObject(name = "MEMBER409_2", value = """
                                    {"isSuccess": false, "code": "MEMBER409_2", "message": "이미 사용 중인 이메일입니다.", "result": null}""")
                    }))
    })
    @SecurityRequirements // 공개 API - Swagger 문서에서 자물쇠를 뺀다
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        return ApiResponse.onCreated("회원가입에 성공했습니다.", memberService.signUp(request));
    }

    @Operation(summary = "내 정보 조회", description = "토큰의 주인인 회원의 이메일, 닉네임, 가입일을 돌려줍니다. 토큰이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (COMMON200)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DESC_401,
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "토큰의 회원이 이미 없음 (MEMBER404_1)", content = @Content(mediaType = JSON, examples = @ExampleObject(EX_MEMBER_404)))
    })
    @GetMapping("/me")
    public ApiResponse<MemberInfoResponse> getMyInfo(
            @LoginMember Long memberId
    ) {
        return ApiResponse.onSuccess("회원 정보를 조회했습니다.", memberService.getMyInfo(memberId));
    }

    @Operation(summary = "내 정보 수정", description = "토큰의 주인인 회원의 닉네임을 바꿉니다. 자기 닉네임을 그대로 보내도 중복으로 막지 않습니다. 토큰이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공 (COMMON200)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 규칙 위반 (COMMON400)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject("""
                            {"isSuccess": false, "code": "COMMON400", "message": "잘못된 요청입니다.", "result": {"nickname": "닉네임은 2자 이상 12자 이하여야 합니다."}}"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = DESC_401,
                    content = @Content(mediaType = JSON, examples = @ExampleObject(EX_401))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "토큰의 회원이 이미 없음 (MEMBER404_1)", content = @Content(mediaType = JSON, examples = @ExampleObject(EX_MEMBER_404))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "다른 회원이 쓰는 닉네임 (MEMBER409_1)",
                    content = @Content(mediaType = JSON, examples = @ExampleObject("""
                            {"isSuccess": false, "code": "MEMBER409_1", "message": "이미 사용 중인 닉네임입니다.", "result": null}""")))
    })
    @PatchMapping("/me")
    public ApiResponse<MemberInfoResponse> updateMyInfo(
            @LoginMember Long memberId,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        return ApiResponse.onSuccess("회원 정보를 수정했습니다.", memberService.updateMyInfo(memberId, request));
    }
}
