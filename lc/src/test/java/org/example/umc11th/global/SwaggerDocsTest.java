package org.example.umc11th.global;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Swagger 문서 경로는 토큰 없이도 열려 있다")
    void apiDocsIsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("공개 API(로그인)는 문서에 자물쇠가 없다")
    void publicApiHasNoLock() throws Exception {
        // @SecurityRequirements를 비워 두면 전체 인증 요구를 덮어써서 operation의 security가 빈 배열이 된다
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/auth/login'].post.security").isEmpty());
    }

    @Test
    @DisplayName("토큰이 필요한 API(즐겨찾기 등록)는 전체 설정의 bearerAuth 자물쇠가 적용된다")
    void protectedApiHasLock() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/favorites'].post.security").doesNotExist());
    }
}
