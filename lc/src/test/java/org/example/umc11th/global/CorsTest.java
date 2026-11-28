package org.example.umc11th.global;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// CORS는 브라우저가 거는 제약이지만, 서버가 허용 목록대로 응답 헤더를 내려주는지는 preflight 요청으로 확인할 수 있다.
@SpringBootTest
@AutoConfigureMockMvc
class CorsTest {

    @Autowired
    private MockMvc mockMvc;

    private org.springframework.test.web.servlet.ResultActions preflight(String origin) throws Exception {
        return mockMvc.perform(options("/favorites")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"));
    }

    @Test
    @DisplayName("설정한 배포 프론트 주소(FRONTEND_ORIGIN)의 preflight 요청은 허용한다")
    void deployedFrontendOriginIsAllowed() throws Exception {
        preflight("https://frontend.example.com")
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://frontend.example.com"));
    }

    @Test
    @DisplayName("로컬 개발용 프론트 주소도 계속 허용한다")
    void localFrontendOriginIsStillAllowed() throws Exception {
        preflight("http://localhost:5173")
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    @DisplayName("허용 목록에 없는 주소의 preflight 요청은 403으로 거절한다")
    void unknownOriginIsRejected() throws Exception {
        preflight("https://evil.example.com")
                .andExpect(status().isForbidden());
    }
}
