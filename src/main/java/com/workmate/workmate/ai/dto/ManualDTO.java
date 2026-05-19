package com.workmate.workmate.ai.dto;

import lombok.Getter;
import lombok.Setter;
import com.workmate.workmate.user.entity.Role;

import io.swagger.v3.oas.annotations.media.Schema;

import com.workmate.workmate.ai.entity.Manual;
import java.util.List;

@Getter
@Setter
@Schema(description = "매뉴얼 DTO")
public class ManualDTO {
    @Schema(description = "매뉴얼 카테고리 ID", example = "1")
    private Long manualId;

    @Schema(description = "매뉴얼 카테고리 제목", example = "메뉴 안내")
    private String title;

    @Schema(description = "근무지 ID", example = "1")
    private Long workplaceId;

    @Schema(description = "매뉴얼을 조회한 사용자의 역할, 매뉴얼 수정 권한에 따른 UI 구현에 사용됨", example = "ADMIN")
    private Role role;

    @Schema(description = "해당 매뉴얼 카테고리의 매뉴얼 목록")
    private List<Manual> manuals;
}
