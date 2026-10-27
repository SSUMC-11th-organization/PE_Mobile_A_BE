package org.example.umc11th.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.member.dto.EmailCheckResponse;
import org.example.umc11th.domain.member.dto.NicknameCheckResponse;
import org.example.umc11th.domain.member.service.MemberService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
