package com.umc.movie.config;

import com.umc.movie.auth.AuthInterceptor;
import com.umc.movie.auth.LoginMemberArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

// 인증 인터셉터를 어느 경로에 적용할지 정합니다. 기본은 "모두 인증 필요"이고, 공개 API만 예외로 둡니다.
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",               // 로그인 (토큰을 받으러 오는 곳)
                        "/members",               // 회원가입 (정확히 /members만, /members/me는 인증 필요)
                        "/members/nickname/**",   // 닉네임 중복 확인
                        "/members/email/**",      // 이메일 중복 확인
                        "/members/*/ratings",     // 유저별 평점 조회 (누구나 볼 수 있음)
                        "/error"                  // 스프링 기본 에러 응답
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
