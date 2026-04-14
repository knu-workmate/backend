package com.workmate.workmate.user.dto;

import lombok.Getter;
import lombok.Setter;
import com.workmate.workmate.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "사용자 프로필 정보 응답 객체")
public class ProfileResponse {
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 역할", example = "WORKER")
    private Role role;

    @Schema(description = "사용자 직장 이름", example = "회사A")
    private String workplaceName;

    public ProfileResponse(String email, String name, Role role, String workplaceName) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.workplaceName = workplaceName;
    }
    
}
