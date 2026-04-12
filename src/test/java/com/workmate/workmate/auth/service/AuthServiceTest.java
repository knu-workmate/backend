package com.workmate.workmate.auth.service;

import com.workmate.workmate.auth.dto.LoginRequest;
import com.workmate.workmate.auth.dto.SignupRequest;
import com.workmate.workmate.auth.dto.SignupResponse;
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
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void signup_validData_returnsSignupResponse() {
        // arrange
        SignupRequest request = new SignupRequest("newuser@example.com", "password123", "홍길동", 0);
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("newuser@example.com");
        savedUser.setName("홍길동");
        savedUser.setRole(Role.WORKER);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // act
        SignupResponse response = authService.signup(request);

        // assert
        assertEquals("회원가입 성공", response.getMessage());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("홍길동", response.getName());
        assertEquals(Role.WORKER, response.getRole());
    }

    @Test
    void signup_duplicateEmail_throwsRuntimeException() {
        // arrange
        SignupRequest request = new SignupRequest("existing@example.com", "password123", "홍길동", 0);
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(existingUser));

        // act / assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.signup(request));

        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    void signup_shortPassword_throwsRuntimeException() {
        // arrange
        SignupRequest request = new SignupRequest("newuser@example.com", "12345", "홍길동", 0);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // act / assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.signup(request));

        assertEquals("비밀번호는 최소 6자 이상이어야 합니다.", exception.getMessage());
    }

    @Test
    void signup_emptyName_throwsRuntimeException() {
        // arrange
        SignupRequest request = new SignupRequest("newuser@example.com", "password123", "", 0);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // act / assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.signup(request));

        assertEquals("이름은 필수 입력 사항입니다.", exception.getMessage());
    }

    @Test
    void signup_nullName_throwsRuntimeException() {
        // arrange
        SignupRequest request = new SignupRequest("newuser@example.com", "password123", null, 0);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // act / assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.signup(request));

        assertEquals("이름은 필수 입력 사항입니다.", exception.getMessage());
    }

    @Test
    void signup_invalidEmailFormat_throwsRuntimeException() {
        // arrange
        SignupRequest request = new SignupRequest("invalid-email", "password123", "홍길동", 0);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // act / assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.signup(request));

        assertEquals("유효한 이메일 형식이 아닙니다.", exception.getMessage());
    }

    @Test
    void signup_invalidRole_throwsRuntimeException() {
        // arrange
        SignupRequest request = new SignupRequest("newuser@example.com", "password123", "홍길동", 2);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // act / assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.signup(request));

        assertEquals("유효한 역할이 아닙니다. 0: WORKER, 1: OWNER", exception.getMessage());
    }
}
