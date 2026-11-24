package com.umc.movie.repository;

import com.umc.movie.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // 내 즐겨찾기를 최신 등록 순으로 (id가 클수록 나중에 등록)
    List<Favorite> findByMember_MemberIdOrderByFavoriteIdDesc(Long memberId);

    // 해제할 때: "내" 즐겨찾기 중에서 이 영화를 찾습니다. 남의 즐겨찾기는 애초에 조회되지 않습니다.
    Optional<Favorite> findByMember_MemberIdAndMovieId(Long memberId, Long movieId);

    // 등록 전 중복 확인, 여부 조회
    boolean existsByMember_MemberIdAndMovieId(Long memberId, Long movieId);
}
