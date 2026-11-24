package com.umc.movie.dto;

import jakarta.validation.constraints.NotBlank;

// POST /auth/login 요청 본문. 형식 검증은 최소한으로 하고, 맞는지 여부는 서비스에서 판단합니다.
public record LoginRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
