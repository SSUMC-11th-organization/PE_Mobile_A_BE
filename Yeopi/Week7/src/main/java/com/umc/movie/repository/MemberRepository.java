package com.umc.movie.repository;

import com.umc.movie.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    // 7주차 로그인: 이메일로 회원을 찾습니다.
    Optional<Member> findByEmail(String email);

    // 7주차 닉네임 변경: 나를 뺀 다른 회원이 이미 쓰고 있는지 확인합니다.
    boolean existsByNicknameAndMemberIdNot(String nickname, Long memberId);
}
