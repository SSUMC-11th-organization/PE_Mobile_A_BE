package com.umc.movie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rating", uniqueConstraints = @UniqueConstraint(
        name = "uk_rating_member_movie", columnNames = {"member_id", "movie_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long ratingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // TMDB 영화 id를 그대로 저장합니다. 영화 테이블이 없으므로 FK가 아닌 일반 컬럼입니다.
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Builder
    public Rating(Member member, Long movieId, Integer score, String comment) {
        this.member = member;
        this.movieId = movieId;
        this.score = score;
        this.comment = comment;
    }

    // 변경 감지(Dirty Checking): 영속 상태에서 값만 바꾸면 트랜잭션 커밋 시 UPDATE가 실행됩니다.
    public void update(Integer score, String comment) {
        this.score = score;
        this.comment = comment;
    }
}
