package com.workmate.workmate.work.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import com.workmate.workmate.work.dto.ScheduleWorker;
import java.time.LocalDate;

@Schema(description = "전체 근무자 스케줄 조회 응답 DTO")
@Getter
@Setter
public class EntireScheduleResponse {
    @Schema(description = "근무 날짜", example = "2024-07-01")
    private LocalDate workDate;

    @Schema(description = "근무자 스케줄 리스트")
    @ArraySchema(schema = @Schema(implementation = ScheduleWorker.class))
    private List<ScheduleWorker> scheduleWorkers;
}
