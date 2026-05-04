package com.workmate.workmate.work.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Schema(description = "스케줄 수정 요청 DTO")
@Getter
@Setter
public class SchedulePatchRequest {
    @NonNull
    @Schema(description = "스케줄 ID", example = "1")
    private Long scheduleId;

    @Schema(description = "새 근무 시작 시간", example = "09:00")
    private LocalTime newStartTime;

    @Schema(description = "새 근무 종료 시간", example = "18:00")
    private LocalTime newEndTime;

    @Schema(description = "새 노트", example = "특이사항 없음")
    private String newNote;
}