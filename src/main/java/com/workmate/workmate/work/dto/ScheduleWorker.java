package com.workmate.workmate.work.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Schema(description = "전체 근무자 스케줄 조회에 List 형태로 사용되는 개별 스케줄 DTO")
@Getter
@Setter
public class ScheduleWorker {
    @Schema(description = "근무자 ID", example = "1")
    private Long userId;

    @Schema(description = "스케줄 ID", example = "1")
    private Long scheduleId;

    @Schema(description = "근무자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "근무 시작 시간", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "근무 종료 시간", example = "18:00")
    private LocalTime endTime;

    @Schema(description = "노트", example = "특이사항 없음")
    private String note;
}
