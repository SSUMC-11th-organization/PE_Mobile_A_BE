package org.example.umc11th.domain.favorite.repository;

import org.example.umc11th.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    // favoriteId 내림차순(최신 등록순)
    List<Favorite> findByMember_MemberIdOrderByFavoriteIdDesc(Long memberId);

    Optional<Favorite> findByMember_MemberIdAndMovieId(Long memberId, Long movieId);

    boolean existsByMember_MemberIdAndMovieId(Long memberId, Long movieId);
}
