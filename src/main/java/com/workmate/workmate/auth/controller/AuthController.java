package com.workmate.workmate.auth.controller;

import org.springframework.web.bind.annotation.*;
import com.workmate.workmate.auth.dto.LoginRequest;
import com.workmate.workmate.auth.dto.TokenResponse;
import com.workmate.workmate.auth.service.AuthService;
import com.workmate.workmate.auth.dto.SignupResponse;
import com.workmate.workmate.auth.dto.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.workmate.workmate.global.security.CurrentUser;

@RestController
@RequestMapping("/auth")
@Tag(name = "인증", description = "사용자 로그인 및 인증 관련 API")
public class AuthController {
    private final AuthService auth;
    private final CurrentUser currentUser;
    public AuthController(AuthService auth, CurrentUser currentUser) {
        this.auth = auth;
        this.currentUser = currentUser;
    }

    @Operation(summary = "사용자 로그인", description = "이메일과 비밀번호로 인증 후 JWT 반환")

    @ApiResponse(responseCode = "200", description = "인증 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class)))

    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))

    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        String token = auth.login(req);
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @Operation(summary = "사용자 회원가입", description = "새로운 사용자 등록, role은 0(WORKER) 또는 1(ADMIN)로 입력")
    @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SignupResponse.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest req) {
        SignupResponse response = auth.signup(req);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "사용자 탈퇴", description = "인증된 사용자가 자신의 계정을 삭제")
    @ApiResponse(responseCode = "200", description = "탈퇴 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw() {
        Long userId = currentUser.getUserId();
        auth.withdraw(userId);
        // 탈퇴 완료 시 "탈퇴 완료되었습니다" 메세지 담아서 return 해주기
        return ResponseEntity.ok("탈퇴 완료되었습니다.");
    }
}
