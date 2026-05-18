package com.workmate.workmate.notification.controller;

import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.notification.dto.ExpoNotificationDto;
import com.workmate.workmate.notification.service.ExpoNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Expo Push Notification", description = "Expo 푸시 알림 저장 및 연동 API")
@RestController
@RequestMapping("/api/expo")
@RequiredArgsConstructor
public class ExpoNotificationController {

    private final ExpoNotificationService expoNotificationService;
    private final CurrentUser currentUser;

    @Operation(summary = "Expo 푸시 토큰 등록/갱신", description = "로그인한 유저의 모바일 푸시 알림용 Expo 토큰을 등록하거나 최신화합니다.")
    @PatchMapping("/push-token") // 최종 URL: PATCH /api/expo/push-token
    public ResponseEntity<Void> updatePushToken(@RequestBody ExpoNotificationDto.PushTokenRequest req) {
        expoNotificationService.updatePushToken(currentUser.getUserId(), req.getToken());
        return ResponseEntity.ok().build();
    }
}