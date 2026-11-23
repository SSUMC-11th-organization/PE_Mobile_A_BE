package org.example.umc11th.domain.favorite.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.favorite.dto.CreateFavoriteRequest;
import org.example.umc11th.domain.favorite.dto.FavoriteResponse;
import org.example.umc11th.domain.favorite.dto.FavoriteStatusResponse;
import org.example.umc11th.domain.favorite.entity.Favorite;
import org.example.umc11th.domain.favorite.exception.FavoriteErrorCode;
import org.example.umc11th.domain.favorite.exception.FavoriteException;
import org.example.umc11th.domain.favorite.repository.FavoriteRepository;
import org.example.umc11th.domain.member.entity.Member;
import org.example.umc11th.domain.member.exception.MemberErrorCode;
import org.example.umc11th.domain.member.exception.MemberException;
import org.example.umc11th.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FavoriteResponse create(Long memberId, CreateFavoriteRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 사전 검사는 정해둔 오류 코드로 응답하기 위한 것이고, 동시 요청까지 막는 것은 DB의 (member_id, movie_id) UNIQUE다
        if (favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new FavoriteException(FavoriteErrorCode.FAVORITE_DUPLICATED);
        }

        Favorite favorite = Favorite.builder()
                .member(member)
                .movieId(request.movieId())
                .build();

        return FavoriteResponse.from(favoriteRepository.save(favorite));
    }

    // 조건에 memberId가 항상 들어가므로 다른 회원의 즐겨찾기는 조회되지 않는다
    public List<FavoriteResponse> getMyFavorites(Long memberId) {
        return favoriteRepository.findByMember_MemberIdOrderByFavoriteIdDesc(memberId).stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    // 즐겨찾기하지 않은 것은 오류가 아니라 정상 상태라서 false로 응답한다
    public FavoriteStatusResponse getStatus(Long memberId, Long movieId) {
        return new FavoriteStatusResponse(
                favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, movieId));
    }

    @Transactional
    public void delete(Long memberId, Long movieId) {
        Favorite favorite = favoriteRepository.findByMember_MemberIdAndMovieId(memberId, movieId)
                .orElseThrow(() -> new FavoriteException(FavoriteErrorCode.FAVORITE_NOT_FOUND));

        favoriteRepository.delete(favorite);
    }
}
