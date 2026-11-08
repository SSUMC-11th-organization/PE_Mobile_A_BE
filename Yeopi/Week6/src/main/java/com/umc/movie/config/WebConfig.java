package com.umc.movie.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 실습 0(CORS 에러 확인)용 스위치: 실행 인자 --app.cors.enabled=false 로 켜면 이 설정이 빠집니다. 기본값은 켜짐.
@Configuration
@ConditionalOnProperty(name = "app.cors.enabled", havingValue = "true", matchIfMissing = true)
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 모든 출처("*") 대신 실제로 쓰는 프론트 출처만 허용합니다.
                // 5173: 간이 프론트(frontend/index.html), 5000: Flutter 웹(--web-port 5000)
                .allowedOrigins("http://localhost:5173", "http://localhost:5000")
                .allowedMethods("GET", "POST", "PATCH", "DELETE")
                .allowedHeaders("*");
    }
}
