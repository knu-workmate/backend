package com.workmate.workmate.notification.controller;

import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.notification.dto.NotificationDto;
import com.workmate.workmate.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification", description = "알림 관련 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUser currentUser; // 현재 로그인된 사용자 정보

    @Operation(summary = "내 알림 목록 조회", description = "로그인한 사용자의 모든 알림 내역을 가져옵니다.")
    @GetMapping
    public ResponseEntity<List<NotificationDto.Response>> getNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications(currentUser.getUserId()));
    }

    @Operation(summary = "안 읽은 알림 개수 조회", description = "읽지 않은 알림이 총 몇 개인지 확인합니다.")
    @GetMapping("/unread-count")
    public ResponseEntity<NotificationDto.UnreadCountResponse> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount(currentUser.getUserId()));
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long notificationId) {
        notificationService.readNotification(notificationId, currentUser.getUserId());
        return ResponseEntity.ok().build();
    }
}