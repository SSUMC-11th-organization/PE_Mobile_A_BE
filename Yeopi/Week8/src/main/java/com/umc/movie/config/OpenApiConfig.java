package com.umc.movie.config;

import com.umc.movie.auth.LoginMember;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// (8주차 선택) Swagger 문서 설정. 실행 후 http://localhost:8080/swagger-ui.html 에서 확인합니다.
// 서버의 기본 규칙(모든 API는 토큰 필요)과 같게, 문서에서도 기본값을 "Bearer 토큰 필요"로 둡니다.
// 공개 API에는 컨트롤러에서 @SecurityRequirements()를 붙여 자물쇠 표시를 뺍니다.
@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    static {
        // @LoginMember Long memberId는 클라이언트가 보내는 값이 아니라 토큰에서 꺼내는 값이므로 문서의 요청 파라미터에서 숨깁니다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(LoginMember.class);
    }

    @Bean
    public OpenAPI movieOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("UMC MovieLog API")
                        .version("week8")
                        .description("""
                                토큰이 필요한 API는 자물쇠 아이콘이 붙어 있습니다.
                                1) POST /auth/login 으로 accessToken을 받고
                                2) 오른쪽 위 Authorize 버튼에 토큰 값만 붙여넣으면(Bearer 는 자동) 모든 요청에 Authorization 헤더가 붙습니다.
                                실패 응답은 { isSuccess: false, code, message, result: null } 형식이며 code로 이유를 구분합니다."""))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
