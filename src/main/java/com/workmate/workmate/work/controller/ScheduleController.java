package com.workmate.workmate.work.controller;

// import annotation
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// import service
import com.workmate.workmate.work.service.ScheduleService;


// import entity, dto
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.work.entity.Schedule;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.global.exception.ErrorResponse;
import com.workmate.workmate.work.dto.ScheduleGetResponse;
import com.workmate.workmate.work.dto.ScheduleRequest;
import com.workmate.workmate.work.dto.ScheduleResponse;

// inport Swagger
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

// import java util
import java.util.List;


@RestController
@RequestMapping("/schedule")
@Tag(name = "스케줄", description = "스케줄 관련 API")
public class ScheduleController {
    private final CurrentUser currentUser;
    private final ScheduleService scheduleService;

    public ScheduleController(CurrentUser currentUser, ScheduleService scheduleService) {
        this.currentUser = currentUser;
        this.scheduleService = scheduleService;
    }

    @PostMapping("/create")
    @Operation(summary = "스케줄 생성", description = "새로운 스케줄을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 생성 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<ScheduleResponse>> createSchedule(@RequestBody List<ScheduleRequest> scheduleRequest) {
        Long userId = currentUser.getUserId();
        List<ScheduleResponse> savedSchedule = scheduleService.saveSchedule(scheduleRequest, userId);
        return ResponseEntity.ok(savedSchedule);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "스케줄 삭제", description = "스케줄을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 삭제 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<ScheduleResponse>> deleteSchedule(@RequestParam List<Long> scheduleId) {
        Long userId = currentUser.getUserId();
        List<ScheduleResponse> deletedSchedule = scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.ok(deletedSchedule);
    }

    // 기간으로 스케줄 조회 (파라미터로 받음)
    @GetMapping("/search")
    @Operation(summary = "기간으로 스케줄 조회", description = "특정 기간 동안의 스케줄을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleGetResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ScheduleGetResponse> getScheduleByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        Long userId = currentUser.getUserId();
        ScheduleGetResponse scheduleGetResponse = scheduleService.getScheduleByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(scheduleGetResponse);
    }
    
}
