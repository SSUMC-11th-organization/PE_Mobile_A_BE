package org.example.umc11th.domain.movierecord.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.favorite.dto.FavoriteResponse;
import org.example.umc11th.domain.favorite.entity.Favorite;
import org.example.umc11th.domain.favorite.exception.FavoriteErrorCode;
import org.example.umc11th.domain.favorite.exception.FavoriteException;
import org.example.umc11th.domain.favorite.repository.FavoriteRepository;
import org.example.umc11th.domain.member.entity.Member;
import org.example.umc11th.domain.member.exception.MemberErrorCode;
import org.example.umc11th.domain.member.exception.MemberException;
import org.example.umc11th.domain.member.repository.MemberRepository;
import org.example.umc11th.domain.movierecord.dto.CreateMovieRecordRequest;
import org.example.umc11th.domain.movierecord.dto.MovieRecordResponse;
import org.example.umc11th.domain.rating.dto.RatingResponse;
import org.example.umc11th.domain.rating.entity.Rating;
import org.example.umc11th.domain.rating.exception.RatingErrorCode;
import org.example.umc11th.domain.rating.exception.RatingException;
import org.example.umc11th.domain.rating.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovieRecordService {

    private final MemberRepository memberRepository;
    private final RatingRepository ratingRepository;
    private final FavoriteRepository favoriteRepository;

    // 평점 저장과 즐겨찾기 저장을 한 트랜잭션으로 묶는다.
    // RatingService.create()와 FavoriteService.create()를 따로 부르면 각자 commit되어, 뒤쪽이 실패해도 평점만 남을 수 있다.
    @Transactional
    public MovieRecordResponse create(Long memberId, CreateMovieRecordRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new RatingException(RatingErrorCode.RATING_DUPLICATED);
        }

        if (request.addToFavorites()
                && favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new FavoriteException(FavoriteErrorCode.FAVORITE_DUPLICATED);
        }

        Rating savedRating = ratingRepository.save(Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build());

        // 여기서 예외가 나면 위에서 저장한 평점까지 함께 rollback된다
        FavoriteResponse favoriteResponse = null;
        if (request.addToFavorites()) {
            Favorite savedFavorite = favoriteRepository.save(Favorite.builder()
                    .member(member)
                    .movieId(request.movieId())
                    .build());
            favoriteResponse = FavoriteResponse.from(savedFavorite);
        }

        return new MovieRecordResponse(RatingResponse.from(savedRating), favoriteResponse);
    }
}
