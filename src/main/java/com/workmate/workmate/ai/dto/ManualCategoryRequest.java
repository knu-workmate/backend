package com.workmate.workmate.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "메뉴얼 카테고리 생성 요청 DTO")
public class ManualCategoryRequest {
    @Schema(description = "카테고리 이름", example = "메뉴 안내")
    private String name;
}
