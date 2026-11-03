package org.example.umc11th.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.member.dto.EmailCheckResponse;
import org.example.umc11th.domain.member.dto.MemberResponse;
import org.example.umc11th.domain.member.dto.NicknameCheckResponse;
import org.example.umc11th.domain.member.dto.SignUpRequest;
import org.example.umc11th.domain.member.entity.Member;
import org.example.umc11th.domain.member.exception.MemberErrorCode;
import org.example.umc11th.domain.member.exception.MemberException;
import org.example.umc11th.domain.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public NicknameCheckResponse checkNickname(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);
        return new NicknameCheckResponse(!exists);   // 이미 있으면 사용 불가
    }

    public EmailCheckResponse checkEmail(String email) {
        boolean exists = memberRepository.existsByEmail(email);
        return new EmailCheckResponse(!exists);
    }

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(MemberErrorCode.MEMBER_EMAIL_DUPLICATED);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new MemberException(MemberErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))   // 평문 대신 bcrypt 해시만 저장
                .nickname(request.nickname())
                .build();

        return MemberResponse.from(memberRepository.save(member));
    }
}
