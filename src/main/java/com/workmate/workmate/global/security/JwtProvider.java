package com.workmate.workmate.global.security;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {
    private final String secret = "thisisaverysecuresecretkey123456"; // 최소 256비트 이상 길이 필요, 추후 환경변수로 관리
    private final long accessTokenValidTime = 1000 * 60 * 60; // 1시간

    private final Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    /**
     * JWT 토큰 생성
     * 
     * @param userId 사용자 ID
     * @param role   사용자 권한
     * @return 생성된 JWT 토큰
     */
    public String createToken(String userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidTime);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

  
    /**
     * JWT 토큰에서 클레임 추출
     * @param token JWT 토큰
     * @return 토큰에서 추출된 클레임 정보
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     * @param token JWT 토큰
     * @return 토큰에서 추출된 사용자 ID
     */
    public Long getUserId(String token) {
       return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * JWT 토큰에서 사용자 권한 추출
     * @param token JWT 토큰
     * @return 토큰에서 추출된 사용자 권한
     */
    public String getRole(String token) {
        return (String) getClaims(token).get("role");
    }

    /**
     * JWT 토큰 유효성 검사
     * @param token JWT 토큰
     * @return 토큰이 유효한 경우 true, 그렇지 않은 경우 false
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
