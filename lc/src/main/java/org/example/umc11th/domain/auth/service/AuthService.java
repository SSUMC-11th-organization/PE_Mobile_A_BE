package org.example.umc11th.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.auth.dto.LoginRequest;
import org.example.umc11th.domain.auth.dto.LoginResponse;
import org.example.umc11th.domain.auth.exception.AuthErrorCode;
import org.example.umc11th.domain.auth.exception.AuthException;
import org.example.umc11th.domain.member.entity.Member;
import org.example.umc11th.domain.member.repository.MemberRepository;
import org.example.umc11th.global.jwt.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // 가입되지 않은 이메일이어도 비밀번호가 틀렸을 때와 같은 예외를 던진다
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        // 저장된 해시에서 salt를 꺼내 입력값을 같은 방식으로 해시한 뒤 비교한다
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());
        return new LoginResponse(member.getNickname(), accessToken);
    }
}
