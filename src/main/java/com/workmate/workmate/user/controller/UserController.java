package com.workmate.workmate.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.workmate.workmate.user.dto.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.workmate.workmate.user.dto.PasswordRequest;


@RestController
@RequestMapping("/user")
@Tag(name = "사용자", description = "사용자 관련 API")
public class UserController {
    CurrentUser currentUser;
    private final UserService userService;

    public UserController(CurrentUser currentUser, UserService userService) {
        this.currentUser = currentUser;
        this.userService = userService;
    }


    @GetMapping("/profile")
    @Operation(summary = "사용자 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<ProfileResponse> getProfile() {
        // 현재 로그인한 사용자의 정보를 반환하는 예시
        Long userId = currentUser.getUserId();
        // userService를 사용하여 사용자 정보를 조회
        ProfileResponse profileResponse = userService.getProfile(userId);
        return ResponseEntity.ok(profileResponse);
    }

    @PatchMapping("/profile")
    @Operation(summary = "사용자 프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<ProfileResponse> updateProfile(@RequestBody ProfileResponse profileRequest) {
        // 현재 로그인한 사용자의 정보를 수정하는 예시
        Long userId = currentUser.getUserId();
        ProfileResponse updatedProfile = userService.updateProfile(userId, profileRequest);
        return ResponseEntity.ok(updatedProfile);
    }
    
    @PatchMapping("/password")
    @Operation(summary = "사용자 비밀번호 수정", description = "현재 로그인한 사용자의 비밀번호를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<String> updatePassword(@RequestBody PasswordRequest passwordRequest) {
        // 현재 로그인한 사용자의 비밀번호를 수정하는 예시
        Long userId = currentUser.getUserId();
        userService.updatePassword(userId, passwordRequest);
        return ResponseEntity.ok("비밀번호가 성공적으로 수정되었습니다.");
    }

}
