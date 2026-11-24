package com.umc.movie.service;

import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.MemberInfoResponse;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.dto.UpdateMemberRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 이미 있으면 사용할 수 없으므로 available = !exists
    public NicknameCheckResponse checkNickname(String nickname) {
        return new NicknameCheckResponse(!memberRepository.existsByNickname(nickname));
    }

    public EmailCheckResponse checkEmail(String email) {
        return new EmailCheckResponse(!memberRepository.existsByEmail(email));
    }

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        // 서비스 검증 — 이메일·닉네임 중복을 각각 다른 code로 알려줍니다.
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        // 위 확인을 동시에 통과한 요청은 DB UNIQUE 제약이 막습니다.
        // saveAndFlush로 INSERT를 바로 실행해 제약 위반을 이 메서드 안에서 드러냅니다.
        return MemberResponse.from(memberRepository.saveAndFlush(member));
    }

    // 7주차 GET /members/me: 토큰에서 꺼낸 memberId로 조회합니다.
    // 토큰은 유효하지만 그 사이 회원이 삭제되었다면 404입니다.
    public MemberInfoResponse getMyInfo(Long memberId) {
        return MemberInfoResponse.from(findMember(memberId));
    }

    // 7주차 PATCH /members/me: 내 닉네임 변경. 지금 내 닉네임과 같으면 그대로 성공합니다.
    @Transactional
    public MemberInfoResponse updateMyInfo(Long memberId, UpdateMemberRequest request) {
        Member member = findMember(memberId);
        if (memberRepository.existsByNicknameAndMemberIdNot(request.nickname(), memberId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        member.updateNickname(request.nickname());
        // 동시에 같은 닉네임으로 바꾸는 경우는 DB UNIQUE 제약이 막습니다. flush로 그 자리에서 드러내 409로 응답합니다.
        memberRepository.flush();
        return MemberInfoResponse.from(member);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
