package com.workmate.workmate.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "매뉴얼 요청 DTO")
public class ManualRequest {
    @Schema(description = "매뉴얼 내용", example = "샷 추가 시 커피 메뉴 이외에는 더블체크 해주세요.")
    private String content;

    @Schema(description = "매뉴얼이 속할 카테고리 ID", example = "1")
    private Long categoryId;
}
