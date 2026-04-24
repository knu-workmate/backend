package com.workmate.workmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 검색 응답 DTO")
public class SearchResponse {
    @Schema(description = "사업장 ID", example = "1")
    private Long id;

    @Schema(description = "사업장 이름", example = "CU 춘천점")
    private String name;

    public SearchResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
