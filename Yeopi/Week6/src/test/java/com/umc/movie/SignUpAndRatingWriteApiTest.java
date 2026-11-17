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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private Long suhyunId;
    private Long minseoId;
    private Long minseoRatingId;

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();
        memberRepository.deleteAll();
        Member suhyun = memberRepository.save(new Member("noco@umc.com", "$2a$10$dummyEncodedPasswordValue", "수현"));
        Member minseo = memberRepository.save(new Member("minseo@umc.com", "$2a$10$dummyEncodedPasswordValue", "민서"));
        minseoRatingId = ratingRepository.save(new Rating(minseo, 550L, 3, "호불호가 갈릴 것 같아요")).getRatingId();
        suhyunId = suhyun.getMemberId();
        minseoId = minseo.getMemberId();
    }

    private static String signUpBody(String email, String password, String nickname) {
        return """
                {"email":"%s","password":"%s","nickname":"%s"}""".formatted(email, password, nickname);
    }

    // ---------- 회원가입 ----------

    @Test
    void 회원가입_성공은_201이고_응답에_password가_없으며_bcrypt로_저장된다() throws Exception {
        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("umc@example.com", "Umc!1234", "moviefan")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.message").value("회원가입에 성공했습니다."))
                .andExpect(jsonPath("$.result.memberId").isNumber())
                .andExpect(jsonPath("$.result.email").value("umc@example.com"))
                .andExpect(jsonPath("$.result.nickname").value("moviefan"))
                .andExpect(jsonPath("$.result.password").doesNotExist());

        String stored = memberRepository.findAll().stream()
                .filter(m -> m.getEmail().equals("umc@example.com")).findFirst().orElseThrow().getPassword();
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

        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("umc@example.com", "Umc!1234", "a")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 2자 이상 12자 이하로 입력해주세요."));

        assertThat(memberRepository.count()).isEqualTo(2);
    }

    @Test
    void Challenge_이메일과_닉네임_중복은_서로_다른_code로_409() throws Exception {
        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("noco@umc.com", "Umc!1234", "새닉네임")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER4091"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));

        mockMvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON)
                        .content(signUpBody("new@umc.com", "Umc!1234", "수현")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER4092"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    void Challenge_동시에_같은_이메일로_가입해도_한_명만_저장되고_나머지는_같은_409_형식이다() throws Exception {
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
            String body = result.getResponse().getContentAsString();
            if (status == 201) {
                created++;
            } else {
                // 사전 중복 체크에 걸렸든, 체크를 통과한 뒤 DB UNIQUE 제약에 걸렸든 같은 code로 응답합니다.
                assertThat(status).isEqualTo(409);
                assertThat(body).contains("\"code\":\"MEMBER4091\"").contains("\"isSuccess\":false");
                conflicts++;
            }
        }
        pool.shutdown();

        assertThat(created).isEqualTo(1);
        assertThat(conflicts).isEqualTo(requests - 1);
        assertThat(memberRepository.findAll()).filteredOn(m -> m.getEmail().equals("race@umc.com")).hasSize(1);
    }

    // ---------- 평점 등록 ----------

    @Test
    void 평점_등록은_201과_ratingId_같은_영화는_409() throws Exception {
        String body = """
                {"movieId":155,"score":4,"comment":"다시 봐도 명작"}""";
        mockMvc.perform(post("/members/{memberId}/ratings", suhyunId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result").isNumber());

        mockMvc.perform(post("/members/{memberId}/ratings", suhyunId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RATING4091"))
                .andExpect(jsonPath("$.message").value("이미 이 영화에 평점을 남기셨습니다."));

        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(suhyunId, 155L)).isTrue();
        assertThat(ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(suhyunId)).hasSize(1);
    }

    @Test
    void 평점_등록_검증_실패는_400_없는_회원은_404() throws Exception {
        mockMvc.perform(post("/members/{memberId}/ratings", suhyunId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":155,\"score\":6}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4001"))
                .andExpect(jsonPath("$.message").value("별점은 5점 이하여야 합니다."));

        mockMvc.perform(post("/members/{memberId}/ratings", suhyunId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":155,\"score\":4,\"comment\":\"" + "가".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("후기는 500자 이하로 입력해주세요."));

        mockMvc.perform(post("/members/{memberId}/ratings", suhyunId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":-1,\"score\":4}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("영화 ID는 양수여야 합니다."));

        mockMvc.perform(post("/members/{memberId}/ratings", 999).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":155,\"score\":4}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEMBER4041"));
    }

    // ---------- 평점 수정 ----------

    @Test
    void 평점_수정은_200이고_조회에_반영된다() throws Exception {
        mockMvc.perform(patch("/members/{memberId}/ratings/{ratingId}", minseoId, minseoRatingId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":5,\"comment\":\"다시 보니 최고\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("평점 수정에 성공했습니다."));

        mockMvc.perform(get("/members/{memberId}/ratings", minseoId))
                .andExpect(jsonPath("$[0].score").value(5))
                .andExpect(jsonPath("$[0].comment").value("다시 보니 최고"));
    }

    @Test
    void 다른_회원의_평점_수정은_403_없는_평점은_404() throws Exception {
        String body = "{\"score\":1,\"comment\":\"남의 평점\"}";
        mockMvc.perform(patch("/members/{memberId}/ratings/{ratingId}", suhyunId, minseoRatingId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RATING4031"));

        mockMvc.perform(patch("/members/{memberId}/ratings/{ratingId}", minseoId, 999)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RATING4041"));

        // 403이었으므로 민서의 평점은 그대로입니다.
        assertThat(ratingRepository.findById(minseoRatingId).orElseThrow().getScore()).isEqualTo(3);
    }

    // ---------- 평점 삭제 ----------

    @Test
    void 평점_삭제는_204이고_목록에서_사라진다_남의_평점은_403() throws Exception {
        mockMvc.perform(delete("/members/{memberId}/ratings/{ratingId}", suhyunId, minseoRatingId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RATING4031"));

        mockMvc.perform(delete("/members/{memberId}/ratings/{ratingId}", minseoId, minseoRatingId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/members/{memberId}/ratings", minseoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(delete("/members/{memberId}/ratings/{ratingId}", minseoId, minseoRatingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RATING4041"));
    }

    @Test
    void JSON_형식이_깨지면_400_같은_형식() throws Exception {
        mockMvc.perform(post("/members/{memberId}/ratings", suhyunId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":155,\"score\":\"다섯\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON4002"));
    }
}
