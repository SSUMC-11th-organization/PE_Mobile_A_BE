package com.umc.movie.util;

import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Duration accessExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") Long accessExpirationSeconds
    ) {
        // 문자열 비밀키를 HMAC-SHA 서명 키로 바꿉니다. 32바이트 미만이면 여기서 예외가 나 서버가 뜨지 않습니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = Duration.ofSeconds(accessExpirationSeconds);
    }

    public String createAccessToken(Long memberId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(memberId))   // 토큰의 주인 = memberId
                .claim("email", email)               // 참고용 정보 (비밀번호처럼 민감한 값은 넣지 않음)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpiration)))
                .signWith(secretKey)                 // 키 길이에 맞는 HS256/384/512로 서명
                .compact();
    }

    // 서명·만료를 검증하고 memberId를 꺼냅니다. 값을 돌려줬다면 검증을 통과한 토큰입니다.
    public Long getMemberId(String token) {
        try {
            return Long.valueOf(parseClaims(token).getSubject());
        } catch (ExpiredJwtException e) {
            // 다시 로그인하면 해결되는 상황이라 따로 구분합니다
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 형식 오류, sub가 숫자가 아닌 경우 등
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)   // 같은 비밀키로 서명을 다시 계산해 비교 (다르면 SignatureException)
                .build()                 // exp가 지났으면 ExpiredJwtException

                .parseSignedClaims(token)
                .getPayload();
    }
}
