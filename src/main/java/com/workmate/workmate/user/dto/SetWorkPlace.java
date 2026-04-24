package com.workmate.workmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "사업장 설정 완료 정보 DTO")
@Getter
public class SetWorkPlace {
    @Schema(description = "사업장 설정 결과 메시지", example = "사업장이 성공적으로 설정되었습니다.")
    private String message;
    @Schema(description = "설정된 사업장 ID", example = "1")
    private Long workplaceId;
    @Schema(description = "설정된 사업장 이름", example = "삼성전자")
    private String workplaceName;
    public SetWorkPlace(String message, Long workplaceId, String workplaceName) {
        this.message = message;
        this.workplaceId = workplaceId;
        this.workplaceName = workplaceName;
    }
}
