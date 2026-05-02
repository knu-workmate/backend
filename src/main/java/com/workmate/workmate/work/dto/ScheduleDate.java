package com.workmate.workmate.work.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Schema(description = "스케줄 날짜 DTO")
public class ScheduleDate {
    @Schema(description = "스케줄 ID", example = "1")
    private Long id;

    @Schema(description = "근무 날짜", example = "2024-07-01")
    private LocalDate workDate;

    @Schema(description = "시작 시간", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "18:00")
    private LocalTime endTime;

    @Schema(description = "메모", example = "n월만 임시 근무")
    private String note;
}
