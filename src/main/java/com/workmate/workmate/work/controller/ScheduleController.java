package com.workmate.workmate.work.controller;

// import annotation
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import com.workmate.workmate.global.exception.ErrorResponse;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.work.dto.ScheduleGetResponse;
import com.workmate.workmate.work.dto.ScheduleRequest;
import com.workmate.workmate.work.dto.ScheduleResponse;
import com.workmate.workmate.work.service.ScheduleService;
import com.workmate.workmate.work.dto.EntireScheduleResponse;
import com.workmate.workmate.work.dto.SchedulePatchRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


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
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<ScheduleResponse>> createSchedule(@RequestBody List<ScheduleRequest> scheduleRequest) {
        Long userId = currentUser.getUserId();
        List<ScheduleResponse> savedSchedule = scheduleService.saveSchedule(scheduleRequest, userId);
        return ResponseEntity.ok(savedSchedule);
    }

    @PostMapping("/create-admin")
    @Operation(summary = "관리자 권한의 스케줄 생성", description = "관리자가 새로운 스케줄을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 생성 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<ScheduleResponse>> createScheduleByAdmin(@RequestBody List<ScheduleRequest> scheduleRequest, @RequestParam Long userId) {
        Long adminUserId = currentUser.getUserId();
        List<ScheduleResponse> savedSchedule = scheduleService.saveScheduleAdmin(scheduleRequest, userId, adminUserId);
        return ResponseEntity.ok(savedSchedule);
    }
    

    @DeleteMapping("/delete")
    @Operation(summary = "스케줄 삭제", description = "스케줄을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 삭제 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<ScheduleResponse>> deleteSchedule(@RequestParam List<Long> scheduleId) {
        Long userId = currentUser.getUserId();
        List<ScheduleResponse> deletedSchedule = scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.ok(deletedSchedule);
    }

    @DeleteMapping("/delete-admin")
    @Operation(summary = "관리자 권한의 스케줄 삭제", description = "관리자가 스케줄을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 삭제 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<ScheduleResponse>> deleteScheduleByAdmin(@RequestParam List<Long> scheduleId) {
        Long adminUserId = currentUser.getUserId();
        List<ScheduleResponse> deletedSchedule = scheduleService.deleteScheduleAdmin(scheduleId, adminUserId);
        return ResponseEntity.ok(deletedSchedule);
    }

    @PatchMapping("/patch")
    @Operation(summary = "스케줄 수정", description = "스케줄을 수정합니다. 날짜는 변경이 불가하며, 시간과 노트만 수정 가능합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ScheduleResponse> updateSchedule(@Valid @RequestBody SchedulePatchRequest schedulePatchRequest) {
        Long userId = currentUser.getUserId();
        ScheduleResponse updatedSchedule = scheduleService.updateSchedule(schedulePatchRequest, userId);
        return ResponseEntity.ok(updatedSchedule);
    }

    @PatchMapping("/patch-admin")
    @Operation(summary = "관리자 권한의 스케줄 수정", description = "관리자가 스케줄을 수정합니다. 날짜는 변경이 불가하며, 시간과 노트만 수정 가능합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ScheduleResponse> updateScheduleByAdmin(@Valid @RequestBody SchedulePatchRequest schedulePatchRequest) {
        Long adminUserId = currentUser.getUserId();
        ScheduleResponse updatedSchedule = scheduleService.updateScheduleAdmin(schedulePatchRequest, adminUserId);
        return ResponseEntity.ok(updatedSchedule);
    }
  

    // 기간으로 스케줄 조회 (파라미터로 받음)
    @GetMapping("/period")
    @Operation(summary = "기간으로 스케줄 조회", description = "특정 기간 동안의 스케줄을 조회합니다. 대타로 인해 변경된 일정을 포함하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleGetResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ScheduleGetResponse> getScheduleByDateRange(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        Long userId = currentUser.getUserId();
        ScheduleGetResponse scheduleGetResponse = scheduleService.getScheduleByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(scheduleGetResponse);
    }

    // 주간 스케줄 조회
    @GetMapping("/week")
    @Operation(summary = "주간 스케줄 조회", description = "주간(월요일~일요일)의 스케줄을 조회합니다. offset으로 이전/이후 주를 조회할 수 있습니다. offset (-1: 이전 주, 0: 이번 주, 1: 다음 주 ... 등)")
    @ApiResponse(responseCode = "200", description = "스케줄 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleGetResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ScheduleGetResponse> getScheduleByWeek(@RequestParam(defaultValue = "0") int offset) {
        Long userId = currentUser.getUserId();
        LocalDate today = LocalDate.now();
        LocalDate baseDate = today.plusWeeks(offset);
        LocalDate startDate = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = baseDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        ScheduleGetResponse scheduleGetResponse = scheduleService.getScheduleByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(scheduleGetResponse);
    }

    // 월간 스케줄 조회
    @GetMapping("/month")
    @Operation(summary = "월간 스케줄 조회", description = "월간(1일~말일)의 스케줄을 조회합니다. offset으로 이전/이후 월을 조회할 수 있습니다. offset (-1: 이전 월, 0: 이번 월, 1: 다음 월 ... 등)")
    @ApiResponse(responseCode = "200", description = "스케줄 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduleGetResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ScheduleGetResponse> getScheduleByMonth(@RequestParam(defaultValue = "0") int offset) {
        Long userId = currentUser.getUserId();
        LocalDate today = LocalDate.now();
        LocalDate baseDate = today.plusMonths(offset);
        LocalDate startDate = baseDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endDate = baseDate.with(TemporalAdjusters.lastDayOfMonth());
        
        ScheduleGetResponse scheduleGetResponse = scheduleService.getScheduleByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(scheduleGetResponse);
    }

    // 특정 기간 동안의 모든 근무자 스케줄 조회
    @GetMapping("/period-all")
    @Operation(summary = "기간으로 모든 근무자 스케줄 조회", description = "특정 기간 동안의 모든 근무자 스케줄을 조회합니다. 대타로 인해 변경된 일정도 포함하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "스케줄 조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EntireScheduleResponse.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<EntireScheduleResponse>> getEntireSchedule(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        Long userId = currentUser.getUserId();
        List<EntireScheduleResponse> entireSchedule = scheduleService.getEntireSchedule(userId, startDate, endDate);
        return ResponseEntity.ok(entireSchedule);
    }
    

    // 한달 동안의 모든 근무자 스케줄 조회
    @GetMapping("/month-all")
    @Operation(summary = "월간 모든 근무자 스케줄 조회", description = "월간(1일~말일) 모든 근무자 스케줄을 조회합니다. offset으로 이전/이후 월을 조회할 수 있습니다. offset (-1: 이전 월, 0: 이번 월, 1: 다음 월 ... 등)")
    @ApiResponse(responseCode = "200", description = "스케줄 조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EntireScheduleResponse.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<EntireScheduleResponse>> getEntireScheduleByMonth(@RequestParam(defaultValue = "0") int offset) {
        Long userId = currentUser.getUserId();
        LocalDate today = LocalDate.now();
        LocalDate baseDate = today.plusMonths(offset);
        LocalDate startDate = baseDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endDate = baseDate.with(TemporalAdjusters.lastDayOfMonth());
        
        List<EntireScheduleResponse> entireSchedule = scheduleService.getEntireSchedule(userId, startDate, endDate);
        return ResponseEntity.ok(entireSchedule);
    }

    // 일주일 동안의 모든 근무자 스케줄 조회
    @GetMapping("/week-all")
    @Operation(summary = "주간 모든 근무자 스케줄 조회", description = "주간(월요일~일요일) 모든 근무자 스케줄을 조회합니다. offset으로 이전/이후 주를 조회할 수 있습니다. offset (-1: 이전 주, 0: 이번 주, 1: 다음 주 ... 등)")
    @ApiResponse(responseCode = "200", description = "스케줄 조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EntireScheduleResponse.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<EntireScheduleResponse>> getEntireScheduleByWeek(@RequestParam(defaultValue = "0") int offset) {
        Long userId = currentUser.getUserId();
        LocalDate today = LocalDate.now();
        LocalDate baseDate = today.plusWeeks(offset);
        LocalDate startDate = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = baseDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        List<EntireScheduleResponse> entireSchedule = scheduleService.getEntireSchedule(userId, startDate, endDate);
        return ResponseEntity.ok(entireSchedule);
    }
}
