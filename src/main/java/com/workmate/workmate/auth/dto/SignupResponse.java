package com.workmate.workmate.auth.dto;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;
import com.workmate.workmate.user.entity.Role;import com.workmate.workmate.user.entity.Role;


@Getter
@Setter
@Schema(description = "회원가입 응답 DTO")
public class SignupResponse {
    @Schema(description = "응답 메시지", example = "회원가입 성공")
    private String message;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    private Role role;

    public SignupResponse(String message, String email, String name, Role role) {
        this.message = message;
        this.email = email;
        this.name = name;
        this.role = role;
    }
    
}
