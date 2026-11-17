package com.umc.movie;

import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 워크북 02_seed.sql과 같은 데이터를 H2에 넣고 API 3개를 실제 흐름(Controller → Service → Repository)으로 확인합니다.
@SpringBootTest
@AutoConfigureMockMvc
class MemberRatingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RatingRepository ratingRepository;

    private Long suhyunId;

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();
        memberRepository.deleteAll();
        Member suhyun = memberRepository.save(new Member("noco@umc.com", "$2a$10$dummyEncodedPasswordValue", "수현"));
        Member minseo = memberRepository.save(new Member("minseo@umc.com", "$2a$10$dummyEncodedPasswordValue", "민서"));
        ratingRepository.save(new Rating(suhyun, 550L, 5, "최고의 영화!"));
        ratingRepository.save(new Rating(suhyun, 155L, 4, "다시 봐도 명작"));
        ratingRepository.save(new Rating(minseo, 550L, 3, "호불호가 갈릴 것 같아요"));
        suhyunId = suhyun.getMemberId();
    }

    @Test
    void 닉네임_중복_체크() throws Exception {
        mockMvc.perform(get("/members/nickname/{nickname}", "수현"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
        mockMvc.perform(get("/members/nickname/{nickname}", "새닉네임"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void 이메일_중복_체크() throws Exception {
        mockMvc.perform(get("/members/email/{email}", "noco@umc.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
        mockMvc.perform(get("/members/email/{email}", "new@umc.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void 유저별_평점을_최신순으로_조회한다() throws Exception {
        mockMvc.perform(get("/members/{memberId}/ratings", suhyunId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].movieId").value(155))
                .andExpect(jsonPath("$[0].score").value(4))
                .andExpect(jsonPath("$[1].movieId").value(550))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].member").doesNotExist());
    }

    @Test
    void 존재하지_않는_유저는_404() throws Exception {
        mockMvc.perform(get("/members/{memberId}/ratings", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 유저입니다."));
    }

    @Test
    void 선택_페이지네이션() throws Exception {
        mockMvc.perform(get("/members/{memberId}/ratings", suhyunId).param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].movieId").value(155));
        mockMvc.perform(get("/members/{memberId}/ratings", suhyunId).param("page", "1").param("size", "1"))
                .andExpect(jsonPath("$[0].movieId").value(550));
    }

    @Test
    void 선택_형식이_틀리면_400() throws Exception {
        mockMvc.perform(get("/members/nickname/{nickname}", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 2~12자여야 합니다."));
        mockMvc.perform(get("/members/email/{email}", "not-an-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));
        mockMvc.perform(get("/members/{memberId}/ratings", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void CORS_허용_출처에만_허용_헤더를_준다() throws Exception {
        mockMvc.perform(get("/members/nickname/{nickname}", "수현").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        mockMvc.perform(options("/members/1/ratings")
                        .header("Origin", "http://localhost:5000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5000"));

        mockMvc.perform(get("/members/nickname/{nickname}", "수현").header("Origin", "http://evil.example.com"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
