package org.example.umc11th.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.umc11th.domain.auth.exception.AuthErrorCode;
import org.example.umc11th.domain.auth.exception.AuthException;
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
            @Value("${jwt.access-expiration}") Long accessExpiration
    ) {
        // 문자열 비밀키를 HMAC-SHA 서명용 키로 바꾼다. 같은 키로만 서명을 만들고 검증할 수 있다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = Duration.ofSeconds(accessExpiration);
    }

    public String createAccessToken(Long memberId, String email) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(memberId))   // 토큰의 주인
                .claim("email", email)               // 참고용 부가 정보 (payload는 누구나 읽을 수 있으니 민감 정보는 넣지 않는다)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpiration)))
                .signWith(secretKey)                 // 내용을 바꾸면 서명이 맞지 않아 검증에서 걸린다
                .compact();
    }

    // 값을 돌려줬다면 서명과 만료 검증까지 통과한 토큰이다.
    public Long getMemberId(String token) {
        try {
            return Long.valueOf(getClaims(token).getPayload().getSubject());
        } catch (ExpiredJwtException e) {
            // 만료는 다시 로그인하면 해결되므로 위조와 구분해서 알려준다
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(60)   // 서버 간 시계 오차를 위해 60초 여유를 둔다
                .build()
                .parseSignedClaims(token);
    }
}
