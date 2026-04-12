package com.workmate.workmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "회원가입 요청 DTO")
public class SignupRequest {
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 비밀번호", example = "password123")
    private String password;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 역할 (0: WORKER, 1: OWNER)", example = "0")
    private int role; // 0: WORKER, 1: OWNER

    public SignupRequest(String email, String password, String name, int role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }
}
