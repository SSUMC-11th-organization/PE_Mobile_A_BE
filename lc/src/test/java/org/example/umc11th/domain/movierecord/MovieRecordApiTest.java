package org.example.umc11th.domain.movierecord;

import org.example.umc11th.domain.favorite.entity.Favorite;
import org.example.umc11th.domain.favorite.repository.FavoriteRepository;
import org.example.umc11th.domain.member.entity.Member;
import org.example.umc11th.domain.member.repository.MemberRepository;
import org.example.umc11th.domain.rating.repository.RatingRepository;
import org.example.umc11th.global.jwt.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 트랜잭션이 되돌리는지 확인하는 테스트라서 테스트 클래스에 @Transactional을 붙이지 않는다.
// 붙이면 테스트 전체가 하나의 트랜잭션이 되어, 서비스의 트랜잭션 동작이 가려진다.
@SpringBootTest
@AutoConfigureMockMvc
class MovieRecordApiTest {

    private static final long MOVIE_ID = 238L;

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

    private Member member;
    private String authorizationHeader;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .email("challenger@umc.com")
                .password("$2a$10$abcdefghijklmnopqrstuv")
                .nickname("영화조아")
                .build());
        authorizationHeader = "Bearer " + jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());
    }

    @AfterEach
    void tearDown() {
        // 실제로 커밋된 데이터라서 직접 지운다 (자식 테이블부터)
        ratingRepository.deleteAll();
        favoriteRepository.deleteAll();
        memberRepository.deleteAll();
    }

    private int ratingCount() {
        return ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(member.getMemberId()).size();
    }

    private int favoriteCount() {
        return favoriteRepository.findByMember_MemberIdOrderByFavoriteIdDesc(member.getMemberId()).size();
    }

    @Test
    @DisplayName("addToFavorites가 true면 평점과 즐겨찾기가 함께 저장된다")
    void createMovieRecordWithFavorite() throws Exception {
        mockMvc.perform(post("/movie-records")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId": 238, "score": 5, "comment": "다시 보고 싶은 영화입니다.", "addToFavorites": true}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.rating.movieId").value(MOVIE_ID))
                .andExpect(jsonPath("$.result.favorite.movieId").value(MOVIE_ID));

        assertThat(ratingCount()).isEqualTo(1);
        assertThat(favoriteCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 즐겨찾기한 영화는 409로 실패하고, 평점도 남지 않는다")
    void createMovieRecordWhenAlreadyFavorite() throws Exception {
        favoriteRepository.save(Favorite.builder().member(member).movieId(MOVIE_ID).build());

        mockMvc.perform(post("/movie-records")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movieId": 238, "score": 5, "comment": "다시 보고 싶은 영화입니다.", "addToFavorites": true}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"));

        // 예외가 났다는 것에서 멈추지 않고, 데이터가 남지 않았는지까지 확인한다
        assertThat(ratingCount()).isZero();
        assertThat(favoriteCount()).isEqualTo(1); // 원래 있던 즐겨찾기 하나뿐
    }
}
