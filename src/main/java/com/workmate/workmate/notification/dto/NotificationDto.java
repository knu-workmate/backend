package com.workmate.workmate.notification.dto;

import com.workmate.workmate.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Schema(description = "알림 관련 데이터 전송 객체")
public class NotificationDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "NotificationResponse", description = "알림 응답 정보")
    public static class Response {

        @Schema(description = "알림 고유 ID", example = "1")
        private Long id;

        @Schema(description = "알림 제목", example = "새 댓글 알림")
        private String title;

        @Schema(description = "알림 상세 내용", example = "우진님이 작성하신 게시글에 새로운 댓글이 달렸습니다.")
        private String content;

        @Schema(description = "읽음 여부", example = "false")
        private Boolean isRead;

        @Schema(description = "알림 생성 일시")
        private LocalDateTime createdAt;

        // 엔티티 -> DTO 변환 메서드
        public static Response fromEntity(Notification notification) {
            return Response.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "NotificationCountResponse", description = "읽지 않은 알림 개수 응답")
    public static class UnreadCountResponse {
        @Schema(description = "읽지 않은 알림의 총 개수", example = "5")
        private Long unreadCount;
    }
}