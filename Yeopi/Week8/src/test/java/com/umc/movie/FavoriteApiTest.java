package com.umc.movie;

import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 8주차 즐겨찾기 API 4개를 워크북 실습 2·4 흐름대로 확인합니다.
@SpringBootTest
@AutoConfigureMockMvc
class FavoriteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Long suhyunId;
    private String suhyunToken;
    private String minseoToken;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        ratingRepository.deleteAll();
        memberRepository.deleteAll();
        Member suhyun = memberRepository.save(new Member("noco@umc.com", "$2a$10$dummyEncodedPasswordValue", "수현"));
        Member minseo = memberRepository.save(new Member("minseo@umc.com", "$2a$10$dummyEncodedPasswordValue", "민서"));
        suhyunId = suhyun.getMemberId();
        suhyunToken = "Bearer " + jwtUtil.createAccessToken(suhyunId, suhyun.getEmail());
        minseoToken = "Bearer " + jwtUtil.createAccessToken(minseo.getMemberId(), minseo.getEmail());
    }

    private static String movieBody(long movieId) {
        return "{\"movieId\":" + movieId + "}";
    }

    @Test
    void 워크북_실습2_등록_중복_목록_여부_해제_흐름() throws Exception {
        // 등록 전: isFavorite false
        mockMvc.perform(get("/favorites/{movieId}", 550).header("Authorization", suhyunToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isFavorite").value(false));

        // 1. 등록 → 201
        mockMvc.perform(post("/favorites").header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content(movieBody(550)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.message").value("즐겨찾기를 등록했습니다."))
                .andExpect(jsonPath("$.result.favoriteId").isNumber())
                .andExpect(jsonPath("$.result.movieId").value(550))
                .andExpect(jsonPath("$.result.createdAt").exists());

        // 2. 같은 요청 → 409 FAVORITE409_1
        mockMvc.perform(post("/favorites").header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content(movieBody(550)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"))
                .andExpect(jsonPath("$.message").value("이미 이 영화를 즐겨찾기하셨습니다."));

        // 3. 목록 → 1개
        mockMvc.perform(get("/favorites").header("Authorization", suhyunToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 즐겨찾기 목록을 조회했습니다."))
                .andExpect(jsonPath("$.result", hasSize(1)))
                .andExpect(jsonPath("$.result[0].movieId").value(550));

        // 4. 여부 → true
        mockMvc.perform(get("/favorites/{movieId}", 550).header("Authorization", suhyunToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("즐겨찾기 여부를 조회했습니다."))
                .andExpect(jsonPath("$.result.isFavorite").value(true));

        // 5. 해제 → 200, result null
        mockMvc.perform(delete("/favorites/{movieId}", 550).header("Authorization", suhyunToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("즐겨찾기를 해제했습니다."))
                .andExpect(jsonPath("$.result").isEmpty());

        // 6. 여부 → false, 7. 목록 → []
        mockMvc.perform(get("/favorites/{movieId}", 550).header("Authorization", suhyunToken))
                .andExpect(jsonPath("$.result.isFavorite").value(false));
        mockMvc.perform(get("/favorites").header("Authorization", suhyunToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(0)));
    }

    @Test
    void 목록은_내_것만_최신_등록_순서로_나온다() throws Exception {
        for (long movieId : new long[]{550, 155, 238}) {
            mockMvc.perform(post("/favorites").header("Authorization", suhyunToken)
                    .contentType(MediaType.APPLICATION_JSON).content(movieBody(movieId)));
        }
        mockMvc.perform(post("/favorites").header("Authorization", minseoToken)
                .contentType(MediaType.APPLICATION_JSON).content(movieBody(13)));

        mockMvc.perform(get("/favorites").header("Authorization", suhyunToken))
                .andExpect(jsonPath("$.result", hasSize(3)))
                .andExpect(jsonPath("$.result[0].movieId").value(238))
                .andExpect(jsonPath("$.result[1].movieId").value(155))
                .andExpect(jsonPath("$.result[2].movieId").value(550));

        // 민서가 수현과 같은 영화를 즐겨찾기해도 충돌하지 않습니다 (UNIQUE는 회원+영화 한 쌍)
        mockMvc.perform(post("/favorites").header("Authorization", minseoToken)
                        .contentType(MediaType.APPLICATION_JSON).content(movieBody(550)))
                .andExpect(status().isCreated());
    }

    @Test
    void 실습4_실패_상황() throws Exception {
        // 토큰 없이 → 401 AUTH401_2
        mockMvc.perform(post("/favorites").contentType(MediaType.APPLICATION_JSON).content(movieBody(550)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_2"));
        mockMvc.perform(get("/favorites"))
                .andExpect(status().isUnauthorized());

        // 없는 즐겨찾기 해제 → 404 FAVORITE404_1
        mockMvc.perform(delete("/favorites/{movieId}", 999).header("Authorization", suhyunToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FAVORITE404_1"))
                .andExpect(jsonPath("$.message").value("즐겨찾기한 영화를 찾을 수 없습니다."));

        // movieId 0 / 빠짐 / 경로 0 → 400
        mockMvc.perform(post("/favorites").header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content(movieBody(0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4001"))
                .andExpect(jsonPath("$.message").value("영화 ID는 양수여야 합니다."));
        mockMvc.perform(post("/favorites").header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("영화 ID는 필수입니다."));
        mockMvc.perform(get("/favorites/{movieId}", 0).header("Authorization", suhyunToken))
                .andExpect(status().isBadRequest());

        // 빈 목록 → 200 []
        mockMvc.perform(get("/favorites").header("Authorization", suhyunToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(0)));
    }

    @Test
    void 남의_즐겨찾기는_해제할_수_없다() throws Exception {
        mockMvc.perform(post("/favorites").header("Authorization", suhyunToken)
                .contentType(MediaType.APPLICATION_JSON).content(movieBody(550)));

        // 민서 토큰으로 550 해제 → 민서의 즐겨찾기 중에는 없으므로 404
        mockMvc.perform(delete("/favorites/{movieId}", 550).header("Authorization", minseoToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FAVORITE404_1"));
        assertThat(favoriteRepository.existsByMember_MemberIdAndMovieId(suhyunId, 550L)).isTrue();
    }

    @Test
    void 동시에_같은_영화를_즐겨찾기해도_한_건만_저장되고_나머지는_FAVORITE409_1() throws Exception {
        int requests = 8;
        ExecutorService pool = Executors.newFixedThreadPool(requests);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<MvcResult>> futures = new ArrayList<>();
        for (int i = 0; i < requests; i++) {
            Callable<MvcResult> call = () -> {
                start.await();
                return mockMvc.perform(post("/favorites").header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content(movieBody(777))).andReturn();
            };
            futures.add(pool.submit(call));
        }
        start.countDown();

        int created = 0;
        for (Future<MvcResult> future : futures) {
            MvcResult result = future.get();
            if (result.getResponse().getStatus() == 201) {
                created++;
            } else {
                // 사전 검사에 걸렸든, 사전 검사를 통과한 뒤 DB UNIQUE에 걸렸든 같은 409 FAVORITE409_1
                assertThat(result.getResponse().getStatus()).isEqualTo(409);
                assertThat(result.getResponse().getContentAsString()).contains("\"code\":\"FAVORITE409_1\"");
            }
        }
        pool.shutdown();

        assertThat(created).isEqualTo(1);
        List<Favorite> saved = favoriteRepository.findByMember_MemberIdOrderByFavoriteIdDesc(suhyunId);
        assertThat(saved).hasSize(1);
    }
}
