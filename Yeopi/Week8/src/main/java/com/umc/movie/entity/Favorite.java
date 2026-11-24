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

// 즐겨찾기 = "회원이 이 TMDB 영화를 보관한다"는 관계 한 줄입니다. 영화 자체의 정보는 TMDB에 있습니다.
// 5주차 sql/01_schema.sql의 favorite 테이블과 연결하고, 생성·수정 시각은 BaseTimeEntity에서 물려받습니다.
@Entity
@Table(name = "favorite", uniqueConstraints = @UniqueConstraint(
        name = "uk_favorite_member_movie", columnNames = {"member_id", "movie_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;

    // 즐겨찾기의 주인. 토큰에서 꺼낸 회원으로만 채워집니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // TMDB 영화 id. 우리 DB에 movie 테이블이 없으므로 FK가 아닌 일반 컬럼입니다.
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Builder
    public Favorite(Member member, Long movieId) {
        this.member = member;
        this.movieId = movieId;
    }
}
