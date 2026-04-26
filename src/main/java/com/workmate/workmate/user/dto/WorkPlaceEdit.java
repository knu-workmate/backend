package com.workmate.workmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "사업장 정보 수정 DTO")
@Getter
@Setter
public class WorkPlaceEdit {
    @Schema(description = "사업장 이름", example = "삼성전자")
    private String name;
}
