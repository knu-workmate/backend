package com.workmate.workmate.work.dto;

import org.hibernate.annotations.Array;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "스케줄 조회 응답 DTO")
public class ScheduleGetResponse {
    @Schema(description = "근무자 ID", example = "1")
    private Long userId;

    @Schema(description = "근무자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "근무지 ID", example = "1")
    private Long workplaceId;

    @Schema(description = "근무지 이름", example = "CU 춘천점")
    private String workplaceName;
    
    @Schema(description = "스케줄 날짜 리스트")
    @ArraySchema(schema = @Schema(description = "스케줄 리스트"))
    private List<ScheduleDate> scheduleDates;
}
