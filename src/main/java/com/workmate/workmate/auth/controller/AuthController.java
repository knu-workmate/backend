package com.workmate.workmate.auth.controller;

import org.springframework.web.bind.annotation.*;
import com.workmate.workmate.auth.dto.LoginRequest;
import com.workmate.workmate.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/auth")
@Tag(name = "인증", description = "사용자 로그인 및 인증 관련 API")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @Operation(summary = "사용자 로그인", description = "이메일과 비밀번호를 통해 사용자 인증을 수행하고 JWT 토큰을 반환한다.")
    @ApiResponse(responseCode = "200", description = "인증 성공 시 JWT 토큰 반환",
        content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 이메일 또는 비밀번호 누락)",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    @ApiResponse(responseCode = "401", description = "인증 실패 (예: 사용자 없음 또는 비밀번호 불일치)",
        content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    @PostMapping("/login")
    public ResponseEntity<String> login(@Parameter(description = "로그인 요청 객체로, 이메일과 비밀번호를 포함한다.", required = true) @RequestBody LoginRequest req) {
        String token = auth.login(req);
        return ResponseEntity.ok(token);
    }
}
