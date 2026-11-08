package org.example.umc11th.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.auth.exception.AuthErrorCode;
import org.example.umc11th.domain.auth.exception.AuthException;
import org.example.umc11th.global.jwt.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String MEMBER_ID = "memberId";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    // 컨트롤러 실행 전에 호출된다. 예외를 던지면 요청은 여기서 끝나고 전역 예외 처리기가 응답을 만든다.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 사전 요청(preflight)에는 토큰이 붙지 않으므로 통과시킨다
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new AuthException(AuthErrorCode.TOKEN_MISSING);
        }

        Long memberId = jwtUtil.getMemberId(header.substring(BEARER_PREFIX.length()));
        request.setAttribute(MEMBER_ID, memberId);
        return true;
    }
}
