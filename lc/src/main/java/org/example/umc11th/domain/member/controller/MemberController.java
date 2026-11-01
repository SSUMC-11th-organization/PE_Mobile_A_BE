package org.example.umc11th.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.member.dto.EmailCheckResponse;
import org.example.umc11th.domain.member.dto.MemberResponse;
import org.example.umc11th.domain.member.dto.NicknameCheckResponse;
import org.example.umc11th.domain.member.dto.SignUpRequest;
import org.example.umc11th.domain.member.service.MemberService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/nickname/{nickname}")
    public ApiResponse<NicknameCheckResponse> checkNickname(@PathVariable String nickname) {
        return ApiResponse.onSuccess(memberService.checkNickname(nickname));
    }

    @GetMapping("/email/{email}")
    public ApiResponse<EmailCheckResponse> checkEmail(@PathVariable String email) {
        return ApiResponse.onSuccess(memberService.checkEmail(email));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        return ApiResponse.onCreated("회원가입에 성공했습니다.", memberService.signUp(request));
    }
}
