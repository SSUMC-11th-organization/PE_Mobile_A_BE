package com.umc.movie.dto;

// 로그인 성공 응답(워크북 명세). 클라이언트는 accessToken을 저장해 두고
// 이후 요청마다 Authorization: Bearer {accessToken} 헤더로 보냅니다.
public record LoginResponse(
        String nickname,
        String accessToken
) {
}
