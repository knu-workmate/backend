package com.workmate.workmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "JWT 토큰 응답 DTO")
@Getter
public class TokenResponse {
    @Schema(description = "인증된 사용자를 위한 JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}