package org.example.umc11th.global.config;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.global.auth.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins(
                        "http://localhost:5173", // mock-frontend (실습 0)
                        "http://localhost:5174"  // Flutter Web (flutter run -d chrome --web-port 5174)
                ) // 이 출처에서 오는 요청만 허용
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 전부 잠그고, 로그인 없이 써야 하는 경로만 연다 (새 API를 깜빡해도 잠긴 채로 남는다)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",              // 로그인
                        "/members",              // 회원가입
                        "/members/nickname/**",  // 닉네임 중복 체크
                        "/members/email/**",     // 이메일 중복 체크
                        "/members/*/ratings",    // 유저별 평점 조회 (공개)
                        "/swagger-ui/**",        // API 문서
                        "/v3/api-docs/**",
                        "/error"
                );
    }
}
