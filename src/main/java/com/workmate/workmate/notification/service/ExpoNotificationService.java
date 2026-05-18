package com.workmate.workmate.notification.service;

import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoNotificationService {



    private final UserRepository userRepository; // 유저 정보 업데이트용


    private final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";


     // 1. 사용자의 Expo 푸시 토큰 등록 및 갱신

    @Transactional
    public void updatePushToken(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다."));

        user.setExpoPushToken(token); // JPA 더티 체킹으로 자동 저장
        log.info("유저 ID {}의 Expo 푸시 토큰이 정상적으로 등록되었습니다.", userId);
    }


    //2. 지정된 토큰으로 실시간 모바일 푸시 알림 발송 (외부 API 연동)

    public void sendPush(String targetToken, String title, String body) {
        if (targetToken == null || targetToken.isEmpty()) {
            log.warn("푸시 알림 발송 실패: 대상 유저의 Expo 토큰이 존재하지 않습니다.");
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            // HTTP 헤더 설정 (JSON 명시)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Expo push 전송 규격 매핑
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("to", targetToken);
            requestBody.put("title", title);
            requestBody.put("body", body);
            requestBody.put("sound", "default"); // 스마트폰 알림음 켜기

            // API 호출
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(EXPO_PUSH_URL, entity, String.class);

            log.info("Expo 푸시 서버 전송 성공! 결과: {}", response.getBody());

        } catch (Exception e) {
            log.error("Expo 푸시 알림 전송 중 외부 연동 에러 발생: ", e);
        }
    }
}