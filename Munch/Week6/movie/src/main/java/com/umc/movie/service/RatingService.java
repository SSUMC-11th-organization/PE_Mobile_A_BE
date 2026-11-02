package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingPageResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public RatingPageResponse getRatingsByMember(Long memberId, int page, int size) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return RatingPageResponse.from(
                ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(memberId, PageRequest.of(page, size))
                        .map(RatingResponse::from)
        );
    }

    @Transactional
    public Long create(Long memberId, CreateRatingRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // rating 테이블에는 (member_id, movie_id) 복합 UNIQUE 제약이 걸려 있습니다.
        // 그냥 저장해도 DB가 막아주지만, 우리가 정한 메시지로 응답하기 위해 먼저 확인합니다.
        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RATING);
        }

        Rating rating = Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build();

        return ratingRepository.save(rating).getRatingId();
    }

    @Transactional
    public void update(Long ratingId, Long memberId, UpdateRatingRequest request) {
        Rating rating = findOwnedRating(ratingId, memberId);

        rating.update(request.score(), request.comment());
        ratingRepository.save(rating);
    }

    @Transactional
    public void delete(Long ratingId, Long memberId) {
        Rating rating = findOwnedRating(ratingId, memberId);

        ratingRepository.delete(rating);
    }

    /**
     * 평점을 찾고, 그 평점이 요청자의 것인지까지 확인합니다.
     * 먼저 찾아보고(없으면 404), 찾았으면 주인을 봅니다(내 것이 아니면 403).
     */
    private Rating findOwnedRating(Long ratingId, Long memberId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RATING_NOT_FOUND));

        if (!rating.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.RATING_FORBIDDEN);
        }

        return rating;
    }
}
