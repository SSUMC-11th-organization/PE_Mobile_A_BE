package com.umc.movie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 7주차: 로그인 → 토큰 발급 → 인터셉터 검증 → /members/me 흐름과 401 응답들을 확인합니다.
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest {

    private static final String PASSWORD = "Umc!1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String secret;

    private Long suhyunId;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        ratingRepository.deleteAll();
        memberRepository.deleteAll();
        suhyunId = memberRepository.save(new Member("noco@umc.com", passwordEncoder.encode(PASSWORD), "수현")).getMemberId();
        memberRepository.save(new Member("minseo@umc.com", passwordEncoder.encode(PASSWORD), "민서"));
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).at("/result/accessToken").asText();
    }

    // ---------- 로그인 ----------

    @Test
    void 로그인_성공은_200과_accessToken() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"noco@umc.com\",\"password\":\"Umc!1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."))
                .andExpect(jsonPath("$.result.nickname").value("수현"))
                .andExpect(jsonPath("$.result.accessToken").isString());
    }

    @Test
    void 토큰_payload에는_memberId와_email만_있고_비밀번호는_없다() throws Exception {
        String token = login("noco@umc.com", PASSWORD);
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3); // header.payload.signature

        // payload는 암호화가 아니라 Base64URL 인코딩이라 누구나 디코딩해서 읽을 수 있습니다.
        JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
        assertThat(payload.get("sub").asText()).isEqualTo(String.valueOf(suhyunId));
        assertThat(payload.get("email").asText()).isEqualTo("noco@umc.com");
        assertThat(payload.has("exp")).isTrue();
        assertThat(payload.has("password")).isFalse();
    }

    @Test
    void 없는_이메일과_틀린_비밀번호는_같은_401_응답() throws Exception {
        String unknownEmail = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@umc.com\",\"password\":\"Umc!1234\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_1"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String wrongPassword = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"noco@umc.com\",\"password\":\"Wrong!1234\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        // 응답 본문이 글자 하나까지 같아서, 응답만 보고는 가입된 이메일인지 알 수 없습니다.
        assertThat(wrongPassword).isEqualTo(unknownEmail);
    }

    @Test
    void 로그인_입력이_비어_있으면_400() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"Umc!1234\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4001"))
                .andExpect(jsonPath("$.message").value("이메일을 입력해주세요."));
    }

    // ---------- /members/me ----------

    @Test
    void 로그인한_토큰으로_내_정보를_조회한다() throws Exception {
        String token = login("noco@umc.com", PASSWORD);
        mockMvc.perform(get("/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.email").value("noco@umc.com"))
                .andExpect(jsonPath("$.result.nickname").value("수현"))
                .andExpect(jsonPath("$.result.createdAt").exists())
                .andExpect(jsonPath("$.result.password").doesNotExist());
    }

    @Test
    void 같은_API라도_토큰에_따라_다른_회원이_나온다() throws Exception {
        String minseoToken = login("minseo@umc.com", PASSWORD);
        mockMvc.perform(get("/members/me").header("Authorization", "Bearer " + minseoToken))
                .andExpect(jsonPath("$.result.nickname").value("민서"));
    }

    @Test
    void 토큰이_없거나_Bearer_형식이_아니면_401_AUTH401_2() throws Exception {
        mockMvc.perform(get("/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH401_2"))
                .andExpect(jsonPath("$.message").value("로그인이 필요한 요청입니다."));

        String token = login("noco@umc.com", PASSWORD);
        mockMvc.perform(get("/members/me").header("Authorization", token)) // "Bearer " 빠짐
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_2"));

        mockMvc.perform(post("/ratings").contentType(MediaType.APPLICATION_JSON).content("{\"movieId\":1,\"score\":5}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_2"));
    }

    @Test
    void payload를_고친_토큰은_서명이_맞지_않아_401_AUTH401_3() throws Exception {
        String token = login("noco@umc.com", PASSWORD);
        String[] parts = token.split("\\.");
        // payload의 sub를 다른 회원 번호로 바꾸고, 서명은 그대로 둡니다 (= 위조 시도)
        String forgedPayload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("\"sub\":\"" + suhyunId + "\"", "\"sub\":\"" + (suhyunId + 1) + "\"");
        String forged = parts[0] + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(forgedPayload.getBytes(StandardCharsets.UTF_8))
                + "." + parts[2];
        assertThat(forged).isNotEqualTo(token);

        mockMvc.perform(get("/members/me").header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_3"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));

        mockMvc.perform(get("/members/me").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_3"));
    }

    @Test
    void 다른_비밀키로_서명한_토큰도_401_AUTH401_3() throws Exception {
        JwtUtil otherServer = new JwtUtil("another-server-secret-key-that-is-long-enough-0123", 3600L);
        String token = otherServer.createAccessToken(suhyunId, "noco@umc.com");
        mockMvc.perform(get("/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_3"));
    }

    @Test
    void 만료된_토큰은_401_AUTH401_4() throws Exception {
        // 같은 비밀키, 유효시간 -120초 → 만들자마자 이미 만료된 토큰
        String expired = new JwtUtil(secret, -120L).createAccessToken(suhyunId, "noco@umc.com");
        mockMvc.perform(get("/members/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_4"))
                .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다. 다시 로그인해주세요."));
    }

    @Test
    void 공개_API는_토큰_없이도_호출된다() throws Exception {
        mockMvc.perform(get("/members/nickname/{nickname}", "새닉네임")).andExpect(status().isOk());
        mockMvc.perform(get("/members/{memberId}/ratings", suhyunId)).andExpect(status().isOk());
    }

    // ---------- PATCH /members/me ----------

    @Test
    void 내_닉네임_변경_중복은_409_내_닉네임_그대로는_200() throws Exception {
        String token = "Bearer " + login("noco@umc.com", PASSWORD);

        mockMvc.perform(patch("/members/me").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"영화광\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("내 정보 수정에 성공했습니다."))
                .andExpect(jsonPath("$.result.nickname").value("영화광"));

        mockMvc.perform(patch("/members/me").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"민서\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER409_1"));

        mockMvc.perform(patch("/members/me").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"영화광\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/members/me").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON4001"));

        assertThat(memberRepository.findById(suhyunId).orElseThrow().getNickname()).isEqualTo("영화광");
    }
}
