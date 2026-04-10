package com.workmate.workmate.auth.dto;

import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {
    @Schema(description = "사용자 이메일", example = "user@example.com")
    final String email;

    @Schema(description = "사용자 비밀번호", example = "password123")
    final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
