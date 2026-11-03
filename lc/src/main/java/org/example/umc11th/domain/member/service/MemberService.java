package org.example.umc11th.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.member.dto.EmailCheckResponse;
import org.example.umc11th.domain.member.dto.NicknameCheckResponse;
import org.example.umc11th.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public NicknameCheckResponse checkNickname(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);
        return new NicknameCheckResponse(!exists);   // 이미 있으면 사용 불가
    }

    public EmailCheckResponse checkEmail(String email) {
        boolean exists = memberRepository.existsByEmail(email);
        return new EmailCheckResponse(!exists);
    }
}
