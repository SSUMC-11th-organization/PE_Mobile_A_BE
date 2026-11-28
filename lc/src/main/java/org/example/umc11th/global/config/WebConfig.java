package org.example.umc11th.global.config;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.global.auth.AuthInterceptor;
import org.example.umc11th.global.auth.LoginMemberArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    // 배포된 프론트 주소 (없으면 로컬 개발 주소만 허용). 환경변수 FRONTEND_ORIGIN으로 전달한다.
    @Value("${cors.frontend-origin:}")
    private String frontendOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins(allowedOrigins()) // 이 출처에서 오는 요청만 허용
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    private String[] allowedOrigins() {
        List<String> origins = new ArrayList<>(List.of(
                "http://localhost:5173", // mock-frontend (실습 0)
                "http://localhost:5174"  // Flutter Web (flutter run -d chrome --web-port 5174)
        ));
        String deployed = frontendOrigin == null ? "" : frontendOrigin.trim();
        if (!deployed.isEmpty()) {
            // Origin은 경로 없이 스킴+호스트[+포트]까지만 비교하므로 끝의 /는 떼어 둔다
            origins.add(deployed.endsWith("/") ? deployed.substring(0, deployed.length() - 1) : deployed);
        }
        return origins.toArray(String[]::new);
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
                        "/swagger-ui.html",      // Swagger UI로 보내주는 주소 (/swagger-ui/index.html로 redirect)
                        "/swagger-ui/**",        // API 문서
                        "/v3/api-docs/**",
                        "/error"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
