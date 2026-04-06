package com.workmate.workmate.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.workmate.workmate.WorkmateApplication;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;

@SpringBootTest(classes = WorkmateApplication.class)
@Transactional
@DisplayName("User Repository 테스트")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        testUser = new User();
        testUser.setName("테스트 사용자");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.WORKER);
        testUser.setCreatedAt(LocalDateTime.now());

        anotherUser = new User();
        anotherUser.setName("다른 사용자");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password456");
        anotherUser.setRole(Role.WORKER);
        anotherUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("사용자 저장 및 조회 성공")
    void testSaveAndFindById() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getName(), foundUser.get().getName());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        assertEquals(testUser.getPassword(), foundUser.get().getPassword());
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void testFindByEmailSuccess() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getName(), foundUser.get().getName());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    @DisplayName("이메일로 사용자 조회 실패 - 존재하지 않는 이메일")
    void testFindByEmailNotFound() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("notexist@example.com");

        // Then
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("이름으로 사용자 조회 성공")
    void testFindByNameSuccess() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByName("테스트 사용자");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        assertEquals(testUser.getName(), foundUser.get().getName());
    }

    @Test
    @DisplayName("이름으로 사용자 조회 실패 - 존재하지 않는 이름")
    void testFindByNameNotFound() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByName("존재하지않는사용자");

        // Then
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("모든 사용자 조회")
    void testFindAll() {
        // Given
        userRepository.save(testUser);
        userRepository.save(anotherUser);

        // When
        var allUsers = userRepository.findAll();

        // Then
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.stream()
                .anyMatch(user -> user.getEmail().equals("test@example.com")));
        assertTrue(allUsers.stream()
                .anyMatch(user -> user.getEmail().equals("another@example.com")));
    }

    @Test
    @DisplayName("사용자 삭제")
    void testDeleteUser() {
        // Given
        User savedUser = userRepository.save(testUser);
        Long userId = savedUser.getId();

        // When
        userRepository.deleteById(userId);

        // Then
        assertTrue(userRepository.findById(userId).isEmpty());
    }

    @Test
    @DisplayName("사용자 업데이트")
    void testUpdateUser() {
        // Given
        User savedUser = userRepository.save(testUser);
        Long userId = savedUser.getId();

        // When
        Optional<User> foundUser = userRepository.findById(userId);
        assertTrue(foundUser.isPresent());
        
        User userToUpdate = foundUser.get();
        userToUpdate.setName("수정된 사용자");
        userToUpdate.setPassword("newPassword123");
        User updatedUser = userRepository.save(userToUpdate);

        // Then
        Optional<User> verifyUser = userRepository.findById(userId);
        assertTrue(verifyUser.isPresent());
        assertEquals("수정된 사용자", verifyUser.get().getName());
        assertEquals("newPassword123", verifyUser.get().getPassword());
    }

    @Test
    @DisplayName("중복된 이메일로 저장 실패")
    void testSaveDuplicateEmailShouldFail() {
        // Given
        userRepository.save(testUser);

        // When & Then
        User duplicateUser = new User();
        duplicateUser.setName("다른이름");
        duplicateUser.setEmail("test@example.com"); // 중복된 이메일
        duplicateUser.setPassword("password789");
        duplicateUser.setRole(Role.WORKER);
        duplicateUser.setCreatedAt(LocalDateTime.now());

        assertThrows(Exception.class, () -> {
            userRepository.save(duplicateUser);
            userRepository.flush(); // 명시적으로 DB에 반영
        });
    }

    @Test
    @DisplayName("사용자 존재 여부 확인")
    void testExistsById() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        boolean exists = userRepository.existsById(savedUser.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("전체 사용자 수 조회")
    void testCountUsers() {
        // Given
        userRepository.save(testUser);
        userRepository.save(anotherUser);

        // When
        long count = userRepository.count();

        // Then
        assertEquals(2, count);
    }
}
