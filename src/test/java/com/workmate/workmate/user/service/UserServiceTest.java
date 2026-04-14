package com.workmate.workmate.user.service;

import com.workmate.workmate.user.dto.PasswordRequest;
import com.workmate.workmate.user.dto.ProfileResponse;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkplaceRepository workplaceRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getProfile_validUser_returnsProfileResponse() {
        Long userId = 1L;
        Workplace workplace = new Workplace();
        workplace.setName("회사A");

        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setName("홍길동");
        user.setRole(Role.WORKER);
        user.setWorkplace(workplace);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        ProfileResponse response = userService.getProfile(userId);

        assertEquals("user@example.com", response.getEmail());
        assertEquals("홍길동", response.getName());
        assertEquals(Role.WORKER, response.getRole());
        assertEquals("회사A", response.getWorkplaceName());
    }

    @Test
    void getProfile_unknownUser_throwsRuntimeException() {
        Long userId = 999L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getProfile(userId));

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void updateProfile_validData_updatesAndReturnsProfileResponse() {
        Long userId = 1L;
        Workplace workplace = new Workplace();
        workplace.setName("회사A");

        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setName("홍길동");
        user.setRole(Role.WORKER);
        user.setWorkplace(workplace);

        ProfileResponse profileRequest = new ProfileResponse(
                "newuser@example.com",
                "김철수",
                Role.ADMIN,
                "회사A"
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willReturn(user);

        ProfileResponse response = userService.updateProfile(userId, profileRequest);

        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("김철수", response.getName());
        assertEquals(Role.ADMIN, response.getRole());
        assertEquals("회사A", response.getWorkplaceName());
    }

    @Test
    void updateProfile_wrongWorkplaceName_throwsRuntimeException() {
        Long userId = 1L;
        Workplace workplace = new Workplace();
        workplace.setName("회사A");

        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setName("홍길동");
        user.setRole(Role.WORKER);
        user.setWorkplace(workplace);

        ProfileResponse profileRequest = new ProfileResponse(
                "user@example.com",
                "홍길동",
                Role.WORKER,
                "다른회사"
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateProfile(userId, profileRequest));

        assertEquals("사업장 이름은 변경할 수 없습니다.", exception.getMessage());
    }

    @Test
    void updatePassword_validData_updatesPassword() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setPassword("current123");

        PasswordRequest request = new PasswordRequest("current123", "newPassword123");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willReturn(user);

        userService.updatePassword(userId, request);

        assertEquals("newPassword123", user.getPassword());
    }

    @Test
    void updatePassword_wrongCurrentPassword_throwsRuntimeException() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setPassword("current123");

        PasswordRequest request = new PasswordRequest("wrong123", "newPassword123");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updatePassword(userId, request));

        assertEquals("현재 비밀번호가 일치하지 않습니다.", exception.getMessage());
    }

    @Test
    void updatePassword_shortNewPassword_throwsRuntimeException() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setPassword("current123");

        PasswordRequest request = new PasswordRequest("current123", "12345");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updatePassword(userId, request));

        assertEquals("새 비밀번호는 최소 6자 이상이어야 합니다.", exception.getMessage());
    }
}
