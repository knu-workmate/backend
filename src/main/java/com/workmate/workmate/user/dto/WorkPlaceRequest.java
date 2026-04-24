package com.workmate.workmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "직장 생성/검색 요청 DTO")
public class WorkPlaceRequest {
    @Schema(description = "직장 이름", example = "삼성전자")
    private String name;

    public WorkPlaceRequest(String name) {
        this.name = name;
    }
}
