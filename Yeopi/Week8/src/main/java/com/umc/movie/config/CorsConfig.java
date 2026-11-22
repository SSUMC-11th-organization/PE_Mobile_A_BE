package com.umc.movie.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS 설정만 따로 둡니다. --app.cors.enabled=false 로 실행하면 이 설정만 빠지고 인증 인터셉터는 그대로 동작합니다.
@Configuration
@ConditionalOnProperty(name = "app.cors.enabled", havingValue = "true", matchIfMissing = true)
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 5173: 간이 프론트(frontend/index.html), 5000: Flutter 웹(--web-port 5000)
                .allowedOrigins("http://localhost:5173", "http://localhost:5000")
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                // 7주차: 브라우저가 Authorization 헤더를 보낼 수 있어야 합니다.
                .allowedHeaders("Authorization", "Content-Type");
    }
}
