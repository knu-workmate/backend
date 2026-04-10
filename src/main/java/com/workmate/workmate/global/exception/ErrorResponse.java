package com.workmate.workmate.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답 DTO")
@Getter
@AllArgsConstructor
public class ErrorResponse {
    @Schema(description = "HTTP 상태 코드", example = "400")
    private final int status;

    @Schema(description = "에러 메시지", example = "잘못된 요청입니다.")
    private final String message;

    @Schema(description = "에러 발생 시간", example = "2023-10-01T12:00:00")
    private final String timestamp;
}