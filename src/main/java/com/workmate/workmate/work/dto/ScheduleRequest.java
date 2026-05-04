package com.workmate.workmate.work.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "스케줄 생성 요청 DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRequest {
    // 사용자 id와 근무지 id는 CurrentUser에서 가져오기 때문에 요청 DTO에는 포함하지 않습니다.

    @Schema(description = "근무 날짜", example = "2024-07-01", required = true)
    private LocalDate workDate;

    @Schema(description = "근무 시작 시간", example = "09:00", required = true)
    private LocalTime startTime;

    @Schema(description = "근무 종료 시간", example = "18:00", required = true)
    private LocalTime endTime;

    @Schema(description = "메모", example = "n월만 임시 근무", required = false)
    private String note;
}
