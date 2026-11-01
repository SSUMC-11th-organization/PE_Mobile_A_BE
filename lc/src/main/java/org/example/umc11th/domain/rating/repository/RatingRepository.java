package org.example.umc11th.domain.rating.repository;

import org.example.umc11th.domain.rating.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    // member.memberId로 찾고, ratingId 내림차순(최신순)
    List<Rating> findByMember_MemberIdOrderByRatingIdDesc(Long memberId);

    boolean existsByMember_MemberIdAndMovieId(Long memberId, Long movieId);
}
