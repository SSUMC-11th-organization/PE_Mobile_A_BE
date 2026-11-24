package com.umc.movie.auth;

import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 컨트롤러 앞의 공통 관문: Authorization 헤더의 토큰을 검증하고 memberId를 요청에 담아 둡니다.
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String MEMBER_ID = "memberId";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS preflight(OPTIONS)에는 토큰이 붙지 않으므로 그냥 통과시킵니다.
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.TOKEN_MISSING);
        }

        // 검증에 실패하면 JwtUtil이 401 예외를 던집니다.
        Long memberId = jwtUtil.getMemberId(header.substring(BEARER_PREFIX.length()).trim());
        request.setAttribute(MEMBER_ID, memberId);
        return true;
    }
}
