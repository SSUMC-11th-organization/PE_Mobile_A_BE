package com.umc.movie.service;

import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// "영화 기록" = 평점 + 즐겨찾기를 한 번에 남기는 하나의 사용자 행동.
// RatingService.create()와 FavoriteService.create()를 차례로 부르지 않고, 이 Service 한 곳에서 하나의 트랜잭션으로 묶습니다.
@Service
@RequiredArgsConstructor
public class MovieRecordService {

    private final MemberRepository memberRepository;
    private final RatingRepository ratingRepository;
    private final FavoriteRepository favoriteRepository;

    // 메서드가 정상적으로 끝나면 commit, RuntimeException(BusinessException, DataIntegrityViolationException 포함)이
    // 밖으로 나가면 rollback합니다. 그래서 즐겨찾기 저장이 실패하면 먼저 저장한 평점도 함께 취소됩니다.
    @Transactional
    public MovieRecordResponse create(Long memberId, CreateMovieRecordRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 저장을 시작하기 전에 두 가지 중복을 모두 확인합니다. (이미 알 수 있는 실패로 쓰기를 시작하지 않기)
        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RATING);
        }
        boolean addToFavorites = request.addToFavorites();
        if (addToFavorites && favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_FAVORITE);
        }

        Rating rating = ratingRepository.saveAndFlush(Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build());

        FavoriteResponse favoriteResponse = null;
        if (addToFavorites) {
            // 여기서 실패하면(예: 동시에 들어온 요청이 먼저 즐겨찾기를 저장해 UNIQUE 위반) 위의 rating INSERT도 rollback됩니다.
            Favorite favorite = favoriteRepository.saveAndFlush(Favorite.builder()
                    .member(member)
                    .movieId(request.movieId())
                    .build());
            favoriteResponse = FavoriteResponse.from(favorite);
        }

        return new MovieRecordResponse(RatingResponse.from(rating), favoriteResponse);
    }
}
