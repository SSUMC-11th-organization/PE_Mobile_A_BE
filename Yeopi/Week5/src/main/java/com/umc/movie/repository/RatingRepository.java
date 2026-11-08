package com.umc.movie.repository;

import com.umc.movie.entity.Rating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByMember_MemberIdOrderByRatingIdDesc(Long memberId);

    // [선택] ?page=&size= 페이지네이션
    List<Rating> findByMember_MemberIdOrderByRatingIdDesc(Long memberId, Pageable pageable);
}
