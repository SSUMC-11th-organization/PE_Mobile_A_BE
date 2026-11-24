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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 8주차 실습 3: POST /movie-records가 평점과 즐겨찾기를 하나의 트랜잭션으로 저장하는지 확인합니다.
@SpringBootTest
@AutoConfigureMockMvc
class MovieRecordTransactionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RatingRepository ratingRepository;

    // 진짜 Repository를 감싼 스파이: 평소에는 그대로 동작하고, 필요한 테스트에서만 일부러 실패시킵니다.
    @MockitoSpyBean
    private FavoriteRepository favoriteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Long suhyunId;
    private String token;

    @BeforeEach
    void setUp() {
        reset(favoriteRepository);
        favoriteRepository.deleteAll();
        ratingRepository.deleteAll();
        memberRepository.deleteAll();
        Member suhyun = memberRepository.save(new Member("noco@umc.com", "$2a$10$dummyEncodedPasswordValue", "수현"));
        suhyunId = suhyun.getMemberId();
        token = "Bearer " + jwtUtil.createAccessToken(suhyunId, suhyun.getEmail());
    }

    private static String recordBody(long movieId, boolean addToFavorites) {
        return """
                {"movieId":%d,"score":5,"comment":"다시 보고 싶은 영화입니다.","addToFavorites":%s}"""
                .formatted(movieId, addToFavorites);
    }

    @Test
    void addToFavorites_true면_평점과_즐겨찾기가_함께_저장된다() throws Exception {
        mockMvc.perform(post("/movie-records").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(238, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.message").value("영화 기록을 저장했습니다."))
                .andExpect(jsonPath("$.result.rating.ratingId").isNumber())
                .andExpect(jsonPath("$.result.rating.movieId").value(238))
                .andExpect(jsonPath("$.result.rating.score").value(5))
                .andExpect(jsonPath("$.result.favorite.favoriteId").isNumber())
                .andExpect(jsonPath("$.result.favorite.movieId").value(238));

        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(suhyunId, 238L)).isTrue();
        assertThat(favoriteRepository.existsByMember_MemberIdAndMovieId(suhyunId, 238L)).isTrue();
    }

    @Test
    void addToFavorites_false면_평점만_저장되고_favorite은_null() throws Exception {
        mockMvc.perform(post("/movie-records").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(238, false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.rating.movieId").value(238))
                .andExpect(jsonPath("$.result.favorite").isEmpty());

        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(suhyunId, 238L)).isTrue();
        assertThat(favoriteRepository.existsByMember_MemberIdAndMovieId(suhyunId, 238L)).isFalse();
    }

    @Test
    void 같은_요청을_다시_보내면_RATING409_1이고_새_행이_생기지_않는다() throws Exception {
        mockMvc.perform(post("/movie-records").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(recordBody(238, true)));

        mockMvc.perform(post("/movie-records").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(238, true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RATING409_1"));

        assertThat(ratingRepository.count()).isEqualTo(1);
        assertThat(favoriteRepository.count()).isEqualTo(1);
    }

    @Test
    void 이미_즐겨찾기한_영화면_FAVORITE409_1이고_평점도_저장되지_않는다() throws Exception {
        favoriteRepository.save(Favorite.builder().member(memberRepository.findById(suhyunId).orElseThrow())
                .movieId(238L).build());

        mockMvc.perform(post("/movie-records").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(238, true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"));

        assertThat(ratingRepository.count()).isZero();
    }

    @Test
    void 즐겨찾기_저장이_실패하면_먼저_저장한_평점도_rollback된다() throws Exception {
        // 사전 검사는 통과했는데, 그 사이 동시에 들어온 요청이 먼저 즐겨찾기를 저장해
        // favorite INSERT가 UNIQUE 제약에 걸린 상황을 흉내 냅니다.
        doThrow(new DataIntegrityViolationException("Duplicate entry for key 'uk_favorite_member_movie'"))
                .when(favoriteRepository).saveAndFlush(any(Favorite.class));

        mockMvc.perform(post("/movie-records").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(recordBody(238, true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"));

        // rating INSERT는 이미 실행(saveAndFlush)됐지만, 같은 트랜잭션이 rollback되어 남아 있지 않습니다.
        assertThat(ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(suhyunId)).isEmpty();
        assertThat(favoriteRepository.count()).isZero();
    }

    @Test
    void 검증과_인증_실패() throws Exception {
        mockMvc.perform(post("/movie-records").contentType(MediaType.APPLICATION_JSON).content(recordBody(238, true)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_2"));

        mockMvc.perform(post("/movie-records").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":238,\"score\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("즐겨찾기 추가 여부는 필수입니다."));

        mockMvc.perform(post("/movie-records").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":238,\"score\":6,\"addToFavorites\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("별점은 5점 이하여야 합니다."));

        assertThat(ratingRepository.count()).isZero();
    }
}
