package com.workmate.workmate.notification.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.workmate.workmate.WorkmateApplication;
import com.workmate.workmate.notification.entity.Notification;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.repository.UserRepository;

@SpringBootTest(classes = WorkmateApplication.class)
@Transactional
@DisplayName("Notification Repository 테스트")
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private Notification notificationA;
    private Notification notificationB;

    @BeforeEach
    void setUp() {
        userA = new User();
        userA.setName("사용자 A");
        userA.setEmail("usera@example.com");
        userA.setPassword("passwordA");
        userA.setRole(Role.WORKER);
        userA = userRepository.save(userA);

        userB = new User();
        userB.setName("사용자 B");
        userB.setEmail("userb@example.com");
        userB.setPassword("passwordB");
        userB.setRole(Role.WORKER);
        userB = userRepository.save(userB);

        notificationA = new Notification();
        notificationA.setUser(userA);
        notificationA.setTitle("새로운 댓글");
        notificationA.setContent("새로운 댓글이 달렸습니다.");
        notificationA.setIsRead(false);

        notificationB = new Notification();
        notificationB.setUser(userA);
        notificationB.setTitle("좋아요");
        notificationB.setContent("게시글이 좋아요를 받았습니다.");
        notificationB.setIsRead(true);
    }

    @Test
    @DisplayName("Notification 저장 및 ID 조회 성공")
    void testSaveAndFindById() {
        Notification saved = notificationRepository.save(notificationA);

        Optional<Notification> found = notificationRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(notificationA.getContent(), found.get().getContent());
        assertEquals(notificationA.getTitle(), found.get().getTitle());
        assertEquals(userA.getId(), found.get().getUser().getId());
        assertFalse(found.get().getIsRead());
    }

    @Test
    @DisplayName("User ID로 Notification 목록 조회 성공")
    void testFindByUserIdSuccess() {
        notificationRepository.save(notificationA);
        notificationRepository.save(notificationB);

        List<Notification> found = notificationRepository.findByUser_Id(userA.getId());

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(n -> "새로운 댓글이 달렸습니다.".equals(n.getContent())));
        assertTrue(found.stream().anyMatch(n -> "게시글이 좋아요를 받았습니다.".equals(n.getContent())));
    }

    @Test
    @DisplayName("읽지 않은 Notification 조회 성공")
    void testFindByUserIdAndIsReadFalseSuccess() {
        notificationRepository.save(notificationA);
        notificationRepository.save(notificationB);

        List<Notification> found = notificationRepository.findByUser_IdAndIsReadFalse(userA.getId());

        assertEquals(1, found.size());
        assertEquals("새로운 댓글이 달렸습니다.", found.get(0).getContent());
        assertFalse(found.get(0).getIsRead());
    }

    @Test
    @DisplayName("읽은 Notification 조회 성공")
    void testFindByUserIdAndIsReadTrueSuccess() {
        notificationRepository.save(notificationA);
        notificationRepository.save(notificationB);

        List<Notification> found = notificationRepository.findByUser_IdAndIsReadTrue(userA.getId());

        assertEquals(1, found.size());
        assertEquals("게시글이 좋아요를 받았습니다.", found.get(0).getContent());
        assertTrue(found.get(0).getIsRead());
    }

    @Test
    @DisplayName("Notification 업데이트")
    void testUpdateNotification() {
        Notification saved = notificationRepository.save(notificationA);
        saved.setContent("수정된 내용");
        saved.setIsRead(true);
        notificationRepository.save(saved);

        Optional<Notification> found = notificationRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("수정된 내용", found.get().getContent());
        assertTrue(found.get().getIsRead());
    }

    @Test
    @DisplayName("Notification 삭제")
    void testDeleteNotification() {
        Notification saved = notificationRepository.save(notificationA);

        notificationRepository.deleteById(saved.getId());

        assertTrue(notificationRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("Notification 수 조회")
    void testCountNotifications() {
        notificationRepository.save(notificationA);
        notificationRepository.save(notificationB);

        assertEquals(2, notificationRepository.count());
    }
}