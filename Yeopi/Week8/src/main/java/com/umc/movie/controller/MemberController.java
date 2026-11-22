package com.umc.movie.controller;

import com.umc.movie.auth.LoginMember;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.MemberInfoResponse;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.dto.UpdateMemberRequest;
import com.umc.movie.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // GET /members/nickname/{nickname}  (공개) 2~12자가 아니면 400
    @SecurityRequirements() // 공개 API (토큰 불필요)
    @GetMapping("/nickname/{nickname}")
    public NicknameCheckResponse checkNickname(
            @PathVariable @Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.") String nickname) {
        return memberService.checkNickname(nickname);
    }

    // GET /members/email/{email}  (공개) 이메일 형식이 아니면 400
    // 이메일의 '.' 뒤가 잘리지 않도록 경로 변수를 정규식으로 끝까지 받습니다.
    @SecurityRequirements() // 공개 API (토큰 불필요)
    @GetMapping("/email/{email:.+}")
    public EmailCheckResponse checkEmail(
            @PathVariable @Email(regexp = ".+@.+\\..+", message = "이메일 형식이 올바르지 않습니다.") String email) {
        return memberService.checkEmail(email);
    }

    // POST /members  (공개) 회원가입 → 201, 응답에는 password가 없습니다.
    @SecurityRequirements() // 공개 API (토큰 불필요)
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        MemberResponse response = memberService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("회원가입에 성공했습니다.", response));
    }

    // GET /members/me → 200 (토큰 필요). 누구인지는 경로가 아니라 토큰으로 판단합니다.
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(@LoginMember Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok("내 정보 조회에 성공했습니다.", memberService.getMyInfo(memberId)));
    }

    // PATCH /members/me → 200 (토큰 필요). 닉네임 변경
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> updateMyInfo(
            @LoginMember Long memberId,
            @Valid @RequestBody UpdateMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("내 정보 수정에 성공했습니다.", memberService.updateMyInfo(memberId, request)));
    }
}
