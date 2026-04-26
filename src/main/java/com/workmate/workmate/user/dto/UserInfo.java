package com.workmate.workmate.user.dto;

import com.workmate.workmate.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 정보 DTO")
public class UserInfo {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 권한", example = "USER/ADMIN")
    private Role role;

    public UserInfo(Long id, String name, Role role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }
}
