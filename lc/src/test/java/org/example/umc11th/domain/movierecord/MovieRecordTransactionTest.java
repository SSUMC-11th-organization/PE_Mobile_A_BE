package org.example.umc11th.domain.movierecord;

import org.example.umc11th.domain.favorite.entity.Favorite;
import org.example.umc11th.domain.favorite.repository.FavoriteRepository;
import org.example.umc11th.domain.member.entity.Member;
import org.example.umc11th.domain.member.repository.MemberRepository;
import org.example.umc11th.domain.movierecord.dto.CreateMovieRecordRequest;
import org.example.umc11th.domain.movierecord.service.MovieRecordService;
import org.example.umc11th.domain.rating.repository.RatingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

// 사전 검사를 모두 통과한 뒤 즐겨찾기 저장에서 실패하는 상황을 만들어, 먼저 저장된 평점이 rollback되는지 확인한다
@SpringBootTest
class MovieRecordTransactionTest {

    private static final Long MOVIE_ID = 99_999_001L;

    @Autowired
    private MovieRecordService movieRecordService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RatingRepository ratingRepository;

    @MockitoBean
    private FavoriteRepository favoriteRepository;

    @Test
    @DisplayName("즐겨찾기 저장이 실패하면 같은 트랜잭션에서 먼저 저장한 평점도 남지 않는다")
    void ratingIsRolledBackWhenFavoriteSaveFails() {
        Member member = memberRepository.findAll().get(0);
        Long memberId = member.getMemberId();
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(anyLong(), anyLong())).willReturn(false);
        given(favoriteRepository.save(any(Favorite.class))).willThrow(new IllegalStateException("즐겨찾기 저장 실패"));

        assertThatThrownBy(() -> movieRecordService.create(
                memberId, new CreateMovieRecordRequest(MOVIE_ID, 5, "rollback 확인", true)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(memberId, MOVIE_ID)).isFalse();
    }
}
