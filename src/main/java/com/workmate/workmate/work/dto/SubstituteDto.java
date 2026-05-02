package com.workmate.workmate.work.dto;

import com.workmate.workmate.work.entity.SubstituteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

public class SubstituteDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "대타 신청 요청")
    public static class CreateRequest {
        @Schema(description = "대상 스케줄 ID")
        private Long scheduleId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "대타 상세 응답")
    public static class Response {
        private Long substituteId;
        private Long scheduleId;
        private String requesterName;
        private String substituteUserName; // 아직 없으면 null
        private LocalDateTime shiftStartTime;
        private LocalDateTime shiftEndTime;
        private SubstituteStatus status;
        private LocalDateTime createdAt;
    }
}