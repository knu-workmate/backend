package com.workmate.workmate.notification.repository;

import com.workmate.workmate.notification.dto.ExpoNotificationDto;
import com.workmate.workmate.notification.service.ExpoNotificationService;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.workmate.workmate.user.entity.Role.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ExpoNotificationTest {

    @Autowired
    private ExpoNotificationService expoNotificationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("1. Expo 푸시 토큰이 유저 테이블에 정상적으로 저장된다")
    void updatePushTokenTest() {

        User user = new User();
        user.setEmail("test@workmate.com");
        user.setName("testman");
        user.setPassword("1234");
        user.setRole(ADMIN);

        User savedUser = userRepository.save(user);
        String mockToken = "ExponentPushToken[woojin_test_123456789]";

        // when: 토큰 등록 서비스 실행
        expoNotificationService.updatePushToken(savedUser.getId(), mockToken);

        // then: DB에 잘 들어갔는지 더티 체킹 및 반영 결과 확인
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getExpoPushToken()).isEqualTo(mockToken);
    }

    @Test
    @DisplayName("2. 가짜 토큰으로 푸시 발송 시 에러 없이 캐치된다")
    void sendPushTest() {
        // given: 가짜 토큰과 메시지 세팅
        String dummyToken = "ExponentPushToken[invalid_dummy_token_for_test]";
        String title = "테스트 알림";
        String body = "테스트 푸시 메시지 본문입니다.";

        // when & then: 메서드를 실행했을 때 서버가 터지지(Crash) 않는지 검증
        expoNotificationService.sendPush(dummyToken, title, body);
    }
}