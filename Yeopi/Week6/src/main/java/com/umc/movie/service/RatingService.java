package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MemberRepository memberRepository;

    // page·size가 없으면 전체, 있으면 해당 페이지만 최신순으로 반환합니다. (5주차)
    public List<RatingResponse> getRatingsByMember(Long memberId, Integer page, Integer size) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        List<Rating> ratings = (page == null && size == null)
                ? ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(memberId)
                : ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(
                        memberId, PageRequest.of(page == null ? 0 : page, size == null ? 10 : size));

        return ratings.stream().map(RatingResponse::from).toList();
    }

    @Transactional
    public Long create(Long memberId, CreateRatingRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // (member_id, movie_id) 복합 UNIQUE가 최종적으로 막아주지만, 정한 메시지로 응답하려고 먼저 확인합니다.
        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RATING);
        }

        Rating rating = Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build();

        return ratingRepository.saveAndFlush(rating).getRatingId();
    }

    @Transactional
    public void update(Long ratingId, Long memberId, UpdateRatingRequest request) {
        Rating rating = findOwnedRating(ratingId, memberId);
        // 영속 엔티티의 값만 바꾸면 커밋 시 변경 감지로 UPDATE가 실행되므로 save()를 다시 부르지 않습니다.
        rating.update(request.score(), request.comment());
    }

    @Transactional
    public void delete(Long ratingId, Long memberId) {
        ratingRepository.delete(findOwnedRating(ratingId, memberId));
    }

    // 수정·삭제 공통: 먼저 찾고(없으면 404), 찾았으면 주인을 확인합니다(내 것이 아니면 403).
    private Rating findOwnedRating(Long ratingId, Long memberId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RATING_NOT_FOUND));

        if (!rating.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.RATING_FORBIDDEN);
        }
        return rating;
    }
}
