package com.workmate.workmate.global.security;

import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("인증 흐름 통합 테스트: 회원 생성 -> 토큰 발행 -> 토큰 파싱 -> CurrentUser 검증")
@Transactional
class CurrentUserIntegrationTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUser currentUser;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() {
        // 1. 테스트 회원 생성
        testUser = new User();
        testUser.setName("테스트 관리자");
        testUser.setEmail("admin@test.com");
        testUser.setPassword("encodedPassword123");
        testUser.setRole(Role.ADMIN);
        testUser.setCreatedAt(LocalDateTime.now());
        
        // DB에 저장
        User savedUser = userRepository.save(testUser);
        testUser = savedUser; // 저장된 User (ID 할당됨)
        
        // 2. 토큰 발행
        testToken = jwtProvider.createToken(
            String.valueOf(testUser.getId()),
            testUser.getRole().toString()
        );
    }

    @Test
    @DisplayName("토큰 생성 후 필수 필드가 포함되어 있는지 검증")
    void testTokenCreation() {
        assertNotNull(testToken, "토큰이 null이 아니어야 합니다.");
        
        // JWT는 세 부분으로 구성됨: header.payload.signature
        String[] parts = testToken.split("\\.");
        assertEquals(3, parts.length, "토큰은 header.payload.signature 형식이어야 합니다.");
    }

    @Test
    @DisplayName("토큰에서 userId를 정상적으로 추출")
    void testUserIdExtraction() {
        Long extractedUserId = jwtProvider.getUserId(testToken);
        
        assertNotNull(extractedUserId, "추출된 userId가 null이 아니어야 합니다.");
        assertEquals(testUser.getId(), extractedUserId, 
            "토큰에서 추출된 userId는 원본 userId와 같아야 합니다.");
    }

    @Test
    @DisplayName("토큰에서 Role을 정상적으로 추출")
    void testRoleExtraction() {
        String extractedRole = jwtProvider.getRole(testToken);
        
        assertNotNull(extractedRole, "추출된 role이 null이 아니어야 합니다.");
        assertEquals("ADMIN", extractedRole, 
            "토큰에서 추출된 role은 ADMIN이어야 합니다.");
    }

    @Test
    @DisplayName("생성한 토큰의 유효성이 true인지 검증")
    void testTokenValidation() {
        boolean isValid = jwtProvider.validateToken(testToken);
        
        assertTrue(isValid, "생성한 토큰은 유효해야 합니다.");
    }

    @Test
    @DisplayName("유효하지 않은 토큰의 유효성이 false인지 검증")
    void testInvalidTokenValidation() {
        String invalidToken = "invalid.token.here";
        
        boolean isValid = jwtProvider.validateToken(invalidToken);
        
        assertFalse(isValid, "유효하지 않은 토큰은 false를 반환해야 합니다.");
    }

    @Test
    @DisplayName("SecurityContext에 인증 정보 설정 후 CurrentUser에서 userId 조회")
    void testCurrentUserGetUserId() {
        // 토큰에서 추출한 정보로 Authentication 생성
        Long userId = jwtProvider.getUserId(testToken);
        String role = jwtProvider.getRole(testToken);
        
        // SecurityContext에 인증 정보 설정
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(
                userId,  // principal: userId
                null,    // credentials
                Arrays.asList(new SimpleGrantedAuthority(role))  // authorities
            );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // CurrentUser에서 조회
        Long currentUserId = currentUser.getUserId();
        
        assertNotNull(currentUserId, "CurrentUser에서 userId를 올바르게 조회해야 합니다.");
        assertEquals(testUser.getId(), currentUserId, 
            "CurrentUser의 userId는 원본 userId와 같아야 합니다.");
    }

    @Test
    @DisplayName("SecurityContext에 인증 정보 설정 후 CurrentUser에서 UserRole 조회")
    void testCurrentUserGetUserRole() {
        // 토큰에서 추출한 정보로 Authentication 생성
        Long userId = jwtProvider.getUserId(testToken);
        String role = jwtProvider.getRole(testToken);
        
        // SecurityContext에 인증 정보 설정
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(
                userId,  // principal: userId
                null,    // credentials
                Arrays.asList(new SimpleGrantedAuthority(role))  // authorities
            );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // CurrentUser에서 조회
        String currentUserRole = currentUser.getUserRole();
        
        assertNotNull(currentUserRole, "CurrentUser에서 UserRole을 올바르게 조회해야 합니다.");
        assertEquals("ADMIN", currentUserRole, 
            "CurrentUser의 role은 ADMIN이어야 합니다.");
    }

    @Test
    @DisplayName("완전한 인증 흐름: 회원 생성 -> 토큰 발행 -> 파싱 -> CurrentUser 검증")
    void testCompleteAuthenticationFlow() {
        // 1. User 생성 및 저장 (setUp에서 수행됨)
        assertNotNull(testUser.getId(), "User ID가 할당되어야 합니다.");
        
        // 2. 토큰 생성
        assertNotNull(testToken, "토큰이 정상적으로 생성되어야 합니다.");
        
        // 3. 토큰 유효성 검증
        assertTrue(jwtProvider.validateToken(testToken), 
            "발행한 토큰은 유효해야 합니다.");
        
        // 4. 토큰에서 userId와 role 추출
        Long extractedUserId = jwtProvider.getUserId(testToken);
        String extractedRole = jwtProvider.getRole(testToken);
        
        assertEquals(testUser.getId(), extractedUserId, 
            "추출된 userId는 원본 userId와 일치해야 합니다.");
        assertEquals("ADMIN", extractedRole, 
            "추출된 role은 원본 role과 일치해야 합니다.");
        
        // 5. SecurityContext에 인증 정보 설정
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(
                extractedUserId,
                null,
                Arrays.asList(new SimpleGrantedAuthority(extractedRole))
            );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // 6. CurrentUser에서 값 검증
        Long currentUserId = currentUser.getUserId();
        String currentUserRole = currentUser.getUserRole();
        
        assertEquals(testUser.getId(), currentUserId, 
            "CurrentUser의 userId는 원본과 일치해야 합니다.");
        assertEquals("ADMIN", currentUserRole, 
            "CurrentUser의 role은 원본과 일치해야 합니다.");
    }

    @Test
    @DisplayName("WORKER 권한으로 동일한 인증 흐름 검증")
    void testCompleteAuthenticationFlowWithWorkerRole() {
        // 1. WORKER 권한의 회원 생성
        User workerUser = new User();
        workerUser.setName("테스트 근로자");
        workerUser.setEmail("worker@test.com");
        workerUser.setPassword("encodedPassword456");
        workerUser.setRole(Role.WORKER);
        workerUser.setCreatedAt(LocalDateTime.now());
        
        User savedWorker = userRepository.save(workerUser);
        
        // 2. WORKER 토큰 생성
        String workerToken = jwtProvider.createToken(
            String.valueOf(savedWorker.getId()),
            Role.WORKER.toString()
        );
        
        // 3. 토큰 검증
        assertTrue(jwtProvider.validateToken(workerToken), 
            "WORKER 토큰도 유효해야 합니다.");
        
        // 4. 토큰에서 정보 추출
        Long extractedUserId = jwtProvider.getUserId(workerToken);
        String extractedRole = jwtProvider.getRole(workerToken);
        
        assertEquals(savedWorker.getId(), extractedUserId);
        assertEquals("WORKER", extractedRole);
        
        // 5. SecurityContext 설정
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(
                extractedUserId,
                null,
                Arrays.asList(new SimpleGrantedAuthority(extractedRole))
            );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // 6. CurrentUser 검증
        assertEquals(savedWorker.getId(), currentUser.getUserId());
        assertEquals("WORKER", currentUser.getUserRole());
    }

    @Test
    @DisplayName("인증 정보가 없을 때 CurrentUser.getUserId()는 예외 발생")
    void testCurrentUserThrowsExceptionWhenNoAuthentication() {
        // SecurityContext 초기화 (인증 정보 제거)
        SecurityContextHolder.clearContext();
        
        assertThrows(RuntimeException.class, 
            () -> currentUser.getUserId(),
            "인증 정보가 없을 때 RuntimeException이 발생해야 합니다.");
    }

    @Test
    @DisplayName("인증 정보가 없을 때 CurrentUser.getUserRole()은 예외 발생")
    void testCurrentUserRoleThrowsExceptionWhenNoAuthentication() {
        // SecurityContext 초기화 (인증 정보 제거)
        SecurityContextHolder.clearContext();
        
        assertThrows(RuntimeException.class, 
            () -> currentUser.getUserRole(),
            "인증 정보가 없을 때 RuntimeException이 발생해야 합니다.");
    }
}
