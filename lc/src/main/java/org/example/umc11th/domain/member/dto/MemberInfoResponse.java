package org.example.umc11th.domain.member.dto;

import org.example.umc11th.domain.member.entity.Member;

import java.time.LocalDateTime;

// 엔티티를 그대로 내보내면 password까지 나가므로 필요한 값만 옮겨 담는다.
public record MemberInfoResponse(
        String email,
        String nickname,
        LocalDateTime createdAt
) {
    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(member.getEmail(), member.getNickname(), member.getCreatedAt());
    }
}
