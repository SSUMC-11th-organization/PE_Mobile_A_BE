package org.example.umc11th.domain.auth.dto;

public record LoginResponse(
        String nickname,
        String accessToken
) {
}
