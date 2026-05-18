package com.workmate.workmate.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "Expo 푸시 알림 관련 데이터 전송 객체")
public class ExpoNotificationDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ExpoPushTokenRequest", description = "Expo 푸시 토큰 등록 요청")
    public static class PushTokenRequest {

        @Schema(description = "프론트엔드에서 앱 구동 시 발급받은 Expo 푸시 토큰",
                example = "ExponentPushToken[exdafdd....]")
        private String token;
    }
}