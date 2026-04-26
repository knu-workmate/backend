package com.workmate.workmate.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.dto.SearchResponse;
import com.workmate.workmate.user.dto.SetWorkPlace;
import com.workmate.workmate.user.dto.UserInfo;
import com.workmate.workmate.user.dto.WorkPlaceRequest;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import com.workmate.workmate.user.service.WorkPlaceService;
import com.workmate.workmate.user.dto.WorkPlaceEdit;
import com.workmate.workmate.user.dto.WorkPlaceInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@RestController
@RequestMapping("/workplace")
@Tag(name = "사업장", description = "사업장 관리 관련 API")
public class WorkPlaceController {
    private final WorkPlaceService workPlaceService;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final WorkplaceRepository workplaceRepository;

    public WorkPlaceController(WorkPlaceService workPlaceService, CurrentUser currentUser, UserRepository userRepository, WorkplaceRepository workplaceRepository) {
        this.workPlaceService = workPlaceService;
        this.currentUser = currentUser;
        this.userRepository = userRepository;
        this.workplaceRepository = workplaceRepository;
    }

    @PostMapping("/create")
    @Operation(summary = "사업장 생성", description = "새로운 사업장을 생성합니다. 관리자 권한이 필요합니다.")
    @ApiResponse(responseCode = "200", description = "사업장 생성 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Workplace.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.UnauthorizedException.class)))
    public ResponseEntity<Workplace> createWorkplace(@RequestBody WorkPlaceRequest request) {
        Workplace createdWorkplace = workPlaceService.createWorkplace(request);
        return ResponseEntity.ok(createdWorkplace); 
    }

    @PatchMapping
    @Operation(summary = "사업장 정보 수정")
    @ApiResponse(responseCode = "200", description = "사업장 정보 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Workplace.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.UnauthorizedException.class)))
    public ResponseEntity<Workplace> updateWorkplace(@RequestBody WorkPlaceEdit request) {
        return ResponseEntity.ok(workPlaceService.updateWorkplace(request));
    }

    @GetMapping("/info")
    @Operation(summary = "사업장 정보 조회", description = "현재 로그인한 사용자의 사업장 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사업장 정보 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkPlaceInfo.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<WorkPlaceInfo> getWorkplaceInfo() {
        WorkPlaceInfo workplaceInfo = workPlaceService.getWorkplaceInfo();
        return ResponseEntity.ok(workplaceInfo); 
    }

    @DeleteMapping
    @Operation(summary = "사업장 삭제")
    @ApiResponse(responseCode = "200", description = "사업장 삭제 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkPlaceInfo.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.UnauthorizedException.class)))
    public ResponseEntity<WorkPlaceInfo> deleteWorkplace() {
        return ResponseEntity.ok(workPlaceService.deleteWorkplace());
    }
    

    @GetMapping("/search")
    @Operation(summary = "사업장 검색", description = "사업장 이름으로 사업장을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "사업장 검색 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SearchResponse.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<List<SearchResponse>> getWorkplaces(@RequestParam String name) {
        List<SearchResponse> workplaces = workPlaceService.getWorkplaces(name);
        return ResponseEntity.ok(workplaces);
    }

    @GetMapping("/users")
    @Operation(summary = "사업장 사용자 조회", description = "사업장에 속한 사용자들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserInfo.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<List<UserInfo>> getWorkplaceUsers() {
        // 유저 id만 조회해 파라미터로 전달
        Long userId = currentUser.getUserId();
        List<UserInfo> userInfos = workPlaceService.getWorkplaceUsers(userId);
        return ResponseEntity.ok(userInfos);
    }

    
    @PatchMapping("/users/{userId}/admin")
    @Operation(summary = "관리자 권한 부여")
    @ApiResponse(responseCode = "200", description = "관리자 권한 부여 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfo.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.UnauthorizedException.class)))
    public ResponseEntity<UserInfo> assignAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(workPlaceService.assignAdmin(currentUser.getUserId(), userId));
    }

    @DeleteMapping("/users/{userId}/admin")
    @Operation(summary = "관리자 권한 제거")
    @ApiResponse(responseCode = "200", description = "관리자 권한 제거 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfo.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.UnauthorizedException.class)))
    public ResponseEntity<UserInfo> removeAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(workPlaceService.removeAdmin(currentUser.getUserId(), userId));
    }

    @DeleteMapping("/leave")
    @Operation(summary = "사업장 탈퇴")
    @ApiResponse(responseCode = "200", description = "사업장 탈퇴 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfo.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<UserInfo> leaveWorkplace() {
        return ResponseEntity.ok(workPlaceService.leaveWorkplace(currentUser.getUserId()));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "근무자 강제 퇴장")
    @ApiResponse(responseCode = "200", description = "근무자 퇴장 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfo.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.UnauthorizedException.class)))
    public ResponseEntity<UserInfo> removeWorker(@PathVariable Long userId) {
        return ResponseEntity.ok(workPlaceService.removeWorker(currentUser.getUserId(), userId));
    }

    // 현재 로그인한 사용자의 사업장 설정
    @PatchMapping("/joinId")
    @Operation(summary = "사업장 설정", description = "현재 로그인한 사용자의 사업장을 사업장 ID로 설정합니다.")
    @ApiResponse(responseCode = "200", description = "사업장 설정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SetWorkPlace.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<SetWorkPlace> setWorkplace(@RequestParam Long workplaceId) {
        Long userId = currentUser.getUserId();
        SetWorkPlace setWorkPlace = workPlaceService.setWorkplaceById(userId, workplaceId);
        return ResponseEntity.ok(setWorkPlace);
    }

    // 현재 로그인한 사용자의 사업장을 초대코드로 설정
    @PatchMapping("/joinCode")
    @Operation(summary = "사업장 초대코드로 설정", description = "현재 로그인한 사용자의 사업장을 초대코드로 설정합니다.")
    @ApiResponse(responseCode = "200", description = "사업장 설정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SetWorkPlace.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<SetWorkPlace> setWorkplaceByCode(@RequestParam String inviteCode) {
        Long userId = currentUser.getUserId();
        SetWorkPlace setWorkPlace = workPlaceService.setWorkplaceByCode(userId, inviteCode);
        return ResponseEntity.ok(setWorkPlace);
    }
    
}
