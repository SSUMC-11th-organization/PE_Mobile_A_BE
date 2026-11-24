package com.umc.movie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// (8주차 선택) Swagger 문서가 토큰 없이 열리고, 인증 필요/공개 API가 구분되어 있는지 확인합니다.
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void API_문서는_토큰_없이_열리고_8주차_API와_인증_구분이_들어_있다() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode doc = objectMapper.readTree(body);

        // 전체 기본값: Bearer 토큰 필요
        assertThat(doc.at("/components/securitySchemes/bearerAuth/scheme").asText()).isEqualTo("bearer");
        assertThat(doc.at("/security/0").has("bearerAuth")).isTrue();

        // 8주차 API 5개
        JsonNode paths = doc.get("paths");
        assertThat(paths.has("/favorites")).isTrue();
        assertThat(paths.has("/favorites/{movieId}")).isTrue();
        assertThat(paths.has("/movie-records")).isTrue();
        assertThat(op(paths, "/favorites", "post").isMissingNode()).isFalse();
        assertThat(op(paths, "/favorites/{movieId}", "delete").isMissingNode()).isFalse();

        // 토큰에서 꺼내는 memberId는 요청 파라미터로 노출되지 않음
        assertThat(op(paths, "/favorites", "get").at("/parameters").toString()).doesNotContain("memberId");
        assertThat(op(paths, "/movie-records", "post").at("/parameters").toString()).doesNotContain("memberId");

        // 실패 응답 code가 문서에 적혀 있음
        assertThat(op(paths, "/favorites", "post").at("/responses/409").toString()).contains("FAVORITE409_1");
        assertThat(op(paths, "/favorites/{movieId}", "delete").at("/responses/404").toString()).contains("FAVORITE404_1");

        // 공개 API는 security가 빈 배열 (자물쇠 없음)
        assertThat(op(paths, "/auth/login", "post").at("/security").isArray()).isTrue();
        assertThat(op(paths, "/auth/login", "post").at("/security")).isEmpty();
        assertThat(op(paths, "/members/{memberId}/ratings", "get").at("/security")).isEmpty();
    }

    // paths의 키("/favorites")에 '/'가 들어 있어서 JSON Pointer 대신 키로 직접 꺼냅니다.
    private static JsonNode op(JsonNode paths, String path, String method) {
        return paths.path(path).path(method);
    }

    @Test
    void Swagger_UI_화면도_토큰_없이_열린다() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
