package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RatingService ratingService;

    private Member createMember(Long memberId, String email, String nickname) {
        Member member = Member.builder()
                .email(email)
                .password("암호화된비밀번호")
                .nickname(nickname)
                .build();
        // memberId는 DB가 만들어주는 값이라 setter가 없으므로, 테스트에서만 직접 넣어줍니다
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }

    @Test
    @DisplayName("이미 평점을 남긴 영화에 다시 평점을 등록하면 RATING409_1 예외를 던지고 저장하지 않는다")
    void createRatingWhenDuplicated() {
        // given: 회원은 존재하고, 이 영화에 이미 평점을 남긴 상황
        Member member = createMember(1L, "challenger@umc.com", "영화조아");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when & then: 409 예외가 나고, 저장은 시도조차 하지 않는다
        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 5, "또 남기기")))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasFieldOrPropertyWithValue("code", "RATING409_1");

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("다른 회원이 남긴 평점을 수정하려 하면 RATING403_1 예외를 던지고 수정하지 않는다")
    void updateOthersRating() {
        // given: 평점 1번은 2번 회원의 것이고, 1번 회원이 수정을 시도하는 상황
        Member owner = createMember(2L, "owner@umc.com", "주인");
        Rating rating = Rating.builder()
                .member(owner)
                .movieId(550L)
                .score(5)
                .comment("원래 후기")
                .build();
        given(ratingRepository.findById(1L)).willReturn(Optional.of(rating));

        // when & then: 403 예외가 나고, 저장(수정 반영)은 시도하지 않는다
        assertThatThrownBy(() -> ratingService.update(1L, 1L, new UpdateRatingRequest(1, "남의 평점 수정")))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN)
                .hasFieldOrPropertyWithValue("code", "RATING403_1");

        verify(ratingRepository, never()).saveAndFlush(any(Rating.class));
    }
}
