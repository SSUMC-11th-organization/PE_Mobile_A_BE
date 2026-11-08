package org.example.umc11th.domain.member.repository;

import org.example.umc11th.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    // 나를 제외하고 이 닉네임을 쓰는 회원이 있는지
    boolean existsByNicknameAndMemberIdNot(String nickname, Long memberId);
}
