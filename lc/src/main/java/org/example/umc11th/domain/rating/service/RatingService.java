package org.example.umc11th.domain.rating.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.member.entity.Member;
import org.example.umc11th.domain.member.exception.MemberErrorCode;
import org.example.umc11th.domain.member.exception.MemberException;
import org.example.umc11th.domain.member.repository.MemberRepository;
import org.example.umc11th.domain.rating.dto.CreateRatingRequest;
import org.example.umc11th.domain.rating.dto.RatingResponse;
import org.example.umc11th.domain.rating.dto.UpdateRatingRequest;
import org.example.umc11th.domain.rating.entity.Rating;
import org.example.umc11th.domain.rating.exception.RatingErrorCode;
import org.example.umc11th.domain.rating.exception.RatingException;
import org.example.umc11th.domain.rating.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MemberRepository memberRepository;

    public List<RatingResponse> getRatingsByMember(Long memberId) {
        // 평점이 0개인 유저와 없는 유저를 구분하기 위해 먼저 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        return ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(memberId).stream()
                .map(RatingResponse::from)
                .toList();
    }

    @Transactional
    public Long create(Long memberId, CreateRatingRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // DB의 (member_id, movie_id) UNIQUE가 최종적으로 막아주지만, 정해둔 메시지로 응답하기 위해 먼저 확인
        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new RatingException(RatingErrorCode.RATING_DUPLICATED);
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
    public void update(Long memberId, Long ratingId, UpdateRatingRequest request) {
        findOwnedRating(memberId, ratingId).update(request.score(), request.comment());
    }

    @Transactional
    public void delete(Long memberId, Long ratingId) {
        ratingRepository.delete(findOwnedRating(memberId, ratingId));
    }

    // 먼저 찾아보고(없으면 404), 찾았으면 주인을 확인한다(내 것이 아니면 403).
    private Rating findOwnedRating(Long memberId, Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RatingException(RatingErrorCode.RATING_NOT_FOUND));

        if (!rating.getMember().getMemberId().equals(memberId)) {
            throw new RatingException(RatingErrorCode.RATING_FORBIDDEN);
        }
        return rating;
    }
}
