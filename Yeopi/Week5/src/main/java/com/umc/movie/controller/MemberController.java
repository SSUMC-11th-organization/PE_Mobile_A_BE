package com.umc.movie.controller;

import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.service.MemberService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // GET /members/nickname/{nickname}  [선택] 2~12자가 아니면 400
    @GetMapping("/nickname/{nickname}")
    public NicknameCheckResponse checkNickname(
            @PathVariable @Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.") String nickname) {
        return memberService.checkNickname(nickname);
    }

    // GET /members/email/{email}  [선택] 이메일 형식이 아니면 400
    // 이메일의 '.' 뒤가 잘리지 않도록 경로 변수를 정규식으로 끝까지 받습니다.
    @GetMapping("/email/{email:.+}")
    public EmailCheckResponse checkEmail(
            @PathVariable @Email(regexp = ".+@.+\\..+", message = "이메일 형식이 올바르지 않습니다.") String email) {
        return memberService.checkEmail(email);
    }
}
