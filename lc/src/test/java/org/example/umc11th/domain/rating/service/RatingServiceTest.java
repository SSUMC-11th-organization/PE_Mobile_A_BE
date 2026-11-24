package org.example.umc11th.domain.rating.service;

import org.example.umc11th.domain.member.entity.Member;
import org.example.umc11th.domain.member.repository.MemberRepository;
import org.example.umc11th.domain.rating.dto.CreateRatingRequest;
import org.example.umc11th.domain.rating.dto.UpdateRatingRequest;
import org.example.umc11th.domain.rating.entity.Rating;
import org.example.umc11th.domain.rating.exception.RatingErrorCode;
import org.example.umc11th.domain.rating.exception.RatingException;
import org.example.umc11th.domain.rating.repository.RatingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    private Member memberOf(long memberId, String nickname) {
        Member member = Member.builder()
                .email(nickname + "@umc.com")
                .password("암호화된비밀번호")
                .nickname(nickname)
                .build();
        ReflectionTestUtils.setField(member, "memberId", memberId); // 저장 전이라 비어 있는 ID를 채워준다
        return member;
    }

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 RATING409_1 예외를 던지고 저장하지 않는다")
    void createRatingWhenDuplicated() {
        // given: 회원 1이 영화 550에 이미 평점을 남겨 둔 상황
        given(memberRepository.findById(1L)).willReturn(Optional.of(memberOf(1L, "owner")));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when & then: 예외가 나고, 저장은 시도조차 하지 않는다
        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 4, "또 봤어요")))
                .isInstanceOfSatisfying(RatingException.class,
                        e -> assertThat(e.getCode()).isEqualTo(RatingErrorCode.RATING_DUPLICATED));

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("남의 평점을 수정하려 하면 RATING403_1 예외를 던지고 평점은 바뀌지 않는다")
    void updateRatingOfOtherMember() {
        // given: 평점 10의 주인은 회원 1인데, 회원 2가 수정을 요청하는 상황
        Rating rating = Rating.builder()
                .member(memberOf(1L, "owner"))
                .movieId(550L)
                .score(5)
                .comment("최고의 영화!")
                .build();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));

        // when & then: 예외가 나고, 별점과 후기는 그대로이고, DB에 반영하는 flush도 일어나지 않는다
        assertThatThrownBy(() -> ratingService.update(2L, 10L, new UpdateRatingRequest(1, "가로채기")))
                .isInstanceOfSatisfying(RatingException.class,
                        e -> assertThat(e.getCode()).isEqualTo(RatingErrorCode.RATING_FORBIDDEN));

        assertThat(rating.getScore()).isEqualTo(5);
        assertThat(rating.getComment()).isEqualTo("최고의 영화!");
        verify(ratingRepository, never()).flush();
    }
}
