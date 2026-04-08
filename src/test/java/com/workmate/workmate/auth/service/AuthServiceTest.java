package com.workmate.workmate.auth.service;

import com.workmate.workmate.auth.dto.LoginRequest;
import com.workmate.workmate.global.security.JwtProvider;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_validCredentials_returnsToken() {
        // arrange
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("password123");
        user.setRole(Role.WORKER);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(jwtProvider.createToken("1", "WORKER")).willReturn("mock-token");

        // act
        String token = authService.login(request);

        // assert
        assertEquals("mock-token", token);
    }

    @Test
    void login_unknownEmail_throwsRuntimeException() {
        // arrange
        LoginRequest request = new LoginRequest("missing@example.com", "password123");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // act / assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void login_invalidPassword_throwsRuntimeException() {
        // arrange
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("password123");
        user.setRole(Role.ADMIN);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));

        // act / assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("비밀번호가 일치하지 않습니다.", exception.getMessage());
    }
}
