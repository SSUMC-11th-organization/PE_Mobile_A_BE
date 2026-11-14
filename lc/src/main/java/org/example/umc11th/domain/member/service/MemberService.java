package org.example.umc11th.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.member.dto.EmailCheckResponse;
import org.example.umc11th.domain.member.dto.MemberInfoResponse;
import org.example.umc11th.domain.member.dto.MemberResponse;
import org.example.umc11th.domain.member.dto.NicknameCheckResponse;
import org.example.umc11th.domain.member.dto.MemberUpdateRequest;
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

    public MemberInfoResponse getMyInfo(Long memberId) {
        return MemberInfoResponse.from(findMember(memberId));
    }

    @Transactional
    public MemberInfoResponse updateMyInfo(Long memberId, MemberUpdateRequest request) {
        Member member = findMember(memberId);

        // 자기 자신이 쓰는 닉네임은 중복으로 보지 않는다
        if (memberRepository.existsByNicknameAndMemberIdNot(request.nickname(), memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        member.updateNickname(request.nickname());
        return MemberInfoResponse.from(member);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
