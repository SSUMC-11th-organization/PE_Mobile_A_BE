package com.umc.movie;

import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 6주차 쓰기 API를 7주차 방식(토큰으로 회원 식별)으로 옮긴 뒤에도 같은 규칙이 지켜지는지 확인합니다.
@SpringBootTest
@AutoConfigureMockMvc
class SignUpAndRatingWriteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private Long suhyunId;
    private Long minseoId;
    private Long minseoRatingId;
    private String suhyunToken;
    private String minseoToken;

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();
        memberRepository.deleteAll();
        Member suhyun = memberRepository.save(new Member("noco@umc.com", "$2a$10$dummyEncodedPasswordValue", "수현"));
        Member minseo = memberRepository.save(new Member("minseo@umc.com", "$2a$10$dummyEncodedPasswordValue", "민서"));
        minseoRatingId = ratingRepository.save(new Rating(minseo, 550L, 3, "호불호가 갈릴 것 같아요")).getRatingId();
        suhyunId = suhyun.getMemberId();
        minseoId = minseo.getMemberId();
        // 로그인 API 자체는 AuthApiTest에서 확인하고, 여기서는 같은 JwtUtil로 바로 토큰을 만듭니다.
        suhyunToken = "Bearer " + jwtUtil.createAccessToken(suhyunId, suhyun.getEmail());
        minseoToken = "Bearer " + jwtUtil.createAccessToken(minseoId, minseo.getEmail());
    }

    private static String signUpBody(String email, String password, String nickname) {
        return """
                {"email":"%s","password":"%s","nickname":"%s"}""".formatted(email, password, nickname);
    }

    // ---------- 회원가입 (공개) ----------

    @Test
    void 회원가입_성공은_201이고_응답에_password가_없으며_bcrypt로_저장된다() throws Exception {
        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("umc@example.com", "Umc!1234", "moviefan")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.memberId").isNumber())
                .andExpect(jsonPath("$.result.email").value("umc@example.com"))
                .andExpect(jsonPath("$.result.password").doesNotExist());

        String stored = memberRepository.findByEmail("umc@example.com").orElseThrow().getPassword();
        assertThat(stored).startsWith("$2a$10$").hasSize(60).isNotEqualTo("Umc!1234");
        assertThat(passwordEncoder.matches("Umc!1234", stored)).isTrue();
    }

    @Test
    void 회원가입_형식이_틀리면_400과_이유를_같은_형식으로_돌려준다() throws Exception {
        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("not-email", "Umc!1234", "moviefan")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON4001"))
                .andExpect(jsonPath("$.message").value("올바른 이메일 형식이 아닙니다."))
                .andExpect(jsonPath("$.result").isEmpty());

        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("umc@example.com", "umc12345", "moviefan")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 대문자/소문자/숫자/특수문자를 포함한 8자 이상이어야 합니다."));

        assertThat(memberRepository.count()).isEqualTo(2);
    }

    @Test
    void 이메일과_닉네임_중복은_서로_다른_code로_409() throws Exception {
        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("noco@umc.com", "Umc!1234", "새닉네임")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER409_2"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));

        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("new@umc.com", "Umc!1234", "수현")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER409_1"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    void 동시에_같은_이메일로_가입해도_한_명만_저장되고_나머지는_같은_409_형식이다() throws Exception {
        int requests = 8;
        ExecutorService pool = Executors.newFixedThreadPool(requests);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<MvcResult>> futures = new ArrayList<>();
        for (int i = 0; i < requests; i++) {
            String nickname = "동시가입" + i;
            Callable<MvcResult> call = () -> {
                start.await();
                return mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("race@umc.com", "Umc!1234", nickname))).andReturn();
            };
            futures.add(pool.submit(call));
        }
        start.countDown();

        int created = 0;
        int conflicts = 0;
        for (Future<MvcResult> future : futures) {
            MvcResult result = future.get();
            int status = result.getResponse().getStatus();
            if (status == 201) {
                created++;
            } else {
                assertThat(status).isEqualTo(409);
                assertThat(result.getResponse().getContentAsString()).contains("\"code\":\"MEMBER409_2\"");
                conflicts++;
            }
        }
        pool.shutdown();

        assertThat(created).isEqualTo(1);
        assertThat(conflicts).isEqualTo(requests - 1);
    }

    // ---------- 평점 등록 (토큰 필요) ----------

    @Test
    void 평점_등록은_토큰의_회원으로_저장되고_201_같은_영화는_409() throws Exception {
        String body = """
                {"movieId":155,"score":4,"comment":"다시 봐도 명작"}""";
        mockMvc.perform(post("/ratings").header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.ratingId").isNumber())
                .andExpect(jsonPath("$.result.movieId").value(155))
                .andExpect(jsonPath("$.result.score").value(4));

        mockMvc.perform(post("/ratings").header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RATING409_1"));

        // 요청 본문·경로 어디에도 memberId가 없지만, 토큰의 주인(수현)으로 저장됩니다.
        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(suhyunId, 155L)).isTrue();
    }

    @Test
    void 평점_등록_검증_실패는_400() throws Exception {
        mockMvc.perform(post("/ratings").header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"movieId\":155,\"score\":6}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4001"))
                .andExpect(jsonPath("$.message").value("별점은 5점 이하여야 합니다."));

        mockMvc.perform(post("/ratings").header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"movieId\":155,\"score\":\"다섯\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4002"));
    }

    @Test
    void 토큰은_유효하지만_회원이_삭제되었으면_404() throws Exception {
        String ghostToken = "Bearer " + jwtUtil.createAccessToken(999L, "ghost@umc.com");
        mockMvc.perform(post("/ratings").header("Authorization", ghostToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"movieId\":155,\"score\":4}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEMBER404_1"));
    }

    // ---------- 평점 수정 ----------

    @Test
    void 평점_수정은_200이고_수정된_평점을_돌려준다() throws Exception {
        mockMvc.perform(patch("/ratings/{ratingId}", minseoRatingId).header("Authorization", minseoToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":5,\"comment\":\"다시 보니 최고\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("평점 수정에 성공했습니다."))
                .andExpect(jsonPath("$.result.ratingId").value(minseoRatingId))
                .andExpect(jsonPath("$.result.score").value(5))
                .andExpect(jsonPath("$.result.comment").value("다시 보니 최고"));

        mockMvc.perform(get("/members/{memberId}/ratings", minseoId))
                .andExpect(jsonPath("$[0].score").value(5));
    }

    @Test
    void 다른_회원의_토큰으로_수정하면_403_없는_평점은_404() throws Exception {
        String body = "{\"score\":1,\"comment\":\"남의 평점\"}";
        mockMvc.perform(patch("/ratings/{ratingId}", minseoRatingId).header("Authorization", suhyunToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RATING403_1"));

        mockMvc.perform(patch("/ratings/{ratingId}", 999).header("Authorization", minseoToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RATING404_1"));

        assertThat(ratingRepository.findById(minseoRatingId).orElseThrow().getScore()).isEqualTo(3);
    }

    // ---------- 평점 삭제 ----------

    @Test
    void 평점_삭제는_200이고_남의_평점은_403_두번째_삭제는_404() throws Exception {
        mockMvc.perform(delete("/ratings/{ratingId}", minseoRatingId).header("Authorization", suhyunToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RATING403_1"));

        mockMvc.perform(delete("/ratings/{ratingId}", minseoRatingId).header("Authorization", minseoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("평점 삭제에 성공했습니다."))
                .andExpect(jsonPath("$.result").isEmpty());

        mockMvc.perform(get("/members/{memberId}/ratings", minseoId))
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(delete("/ratings/{ratingId}", minseoRatingId).header("Authorization", minseoToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RATING404_1"));
    }
}
