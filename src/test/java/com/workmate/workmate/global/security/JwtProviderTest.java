package com.workmate.workmate.global.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider();

    @Test
    void createToken_shouldReturnNonNullToken() {
        String token = jwtProvider.createToken("42", "ADMIN");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "JWT 토큰은 헤더, 페이로드, 서명 세 부분으로 구성되어야 합니다.");
    }

    @Test
    void parseToken_shouldExtractUserIdAndRole() {
        String expectedUserId = "42";
        String expectedRole = "ADMIN";

        String token = jwtProvider.createToken(expectedUserId, expectedRole);

        Long parsedUserId = jwtProvider.getUserId(token);
        String parsedRole = jwtProvider.getRole(token);
        boolean valid = jwtProvider.validateToken(token);

        assertEquals(42L, parsedUserId);
        assertEquals(expectedRole, parsedRole);
        assertTrue(valid, "토큰 유효성 검사는 true여야 합니다.");
    }
}
