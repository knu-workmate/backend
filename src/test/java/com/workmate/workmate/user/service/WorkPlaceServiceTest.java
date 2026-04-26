package com.workmate.workmate.user.service;

import com.workmate.workmate.global.exception.UnauthorizedException;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.dto.SearchResponse;
import com.workmate.workmate.user.dto.WorkPlaceRequest;
import com.workmate.workmate.user.dto.WorkPlaceEdit;
import com.workmate.workmate.user.dto.UserInfo;
import com.workmate.workmate.user.dto.WorkPlaceInfo;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import com.workmate.workmate.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkPlace Service 테스트")
class WorkPlaceServiceTest {

    @Mock
    private WorkplaceRepository workplaceRepository;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkPlaceService workPlaceService;

    @Test
    @DisplayName("직장 생성 성공 - ADMIN 권한")
    void createWorkplace_withAdminRole_success() {
        // Given
        WorkPlaceRequest request = new WorkPlaceRequest("삼성전자");
        Workplace workplace = new Workplace();
        workplace.setId(1L);
        workplace.setName("삼성전자");
        workplace.setInviteCode("SAMSUNG123");

        given(currentUser.getUserRole()).willReturn("ADMIN");
        given(workplaceRepository.save(any(Workplace.class))).willReturn(workplace);

        // When
        Workplace result = workPlaceService.createWorkplace(request);

        // Then
        assertNotNull(result);
        assertEquals("삼성전자", result.getName());
        assertNotNull(result.getInviteCode());
    }

    @Test
    @DisplayName("직장 생성 실패 - WORKER 권한")
    void createWorkplace_withWorkerRole_throwsException() {
        // Given
        WorkPlaceRequest request = new WorkPlaceRequest("삼성전자");
        given(currentUser.getUserRole()).willReturn("WORKER");

        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> workPlaceService.createWorkplace(request));

        assertEquals("관리자 권한이 필요합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("직장 검색 성공")
    void searchWorkplaces_validName_returnsSearchResponses() {
        // Given
        Workplace workplace1 = new Workplace();
        workplace1.setId(1L);
        workplace1.setName("삼성전자");
        workplace1.setInviteCode("SAMSUNG123");

        Workplace workplace2 = new Workplace();
        workplace2.setId(2L);
        workplace2.setName("삼성화학");
        workplace2.setInviteCode("SAMSUNG456");

        List<Workplace> workplaces = Arrays.asList(workplace1, workplace2);
        given(workplaceRepository.findByNameContaining("삼성")).willReturn(workplaces);

        // When
        List<SearchResponse> result = workPlaceService.getWorkplaces("삼성");

        // Then
        assertEquals(2, result.size());
        assertEquals("삼성전자", result.get(0).getName());
        assertEquals("삼성화학", result.get(1).getName());
    }

    @Test
    @DisplayName("직장 검색 - 검색 결과 없음")
    void searchWorkplaces_noResults_returnsEmptyList() {
        // Given
        given(workplaceRepository.findByNameContaining("없는회사")).willReturn(Arrays.asList());

        // When
        List<SearchResponse> result = workPlaceService.getWorkplaces("없는회사");

        // Then
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("초대코드 생성 유효성 확인")
    void createWorkplace_generatesUniqueInvitationCode() {
        // Given
        WorkPlaceRequest request1 = new WorkPlaceRequest("회사1");
        WorkPlaceRequest request2 = new WorkPlaceRequest("회사2");

        given(currentUser.getUserRole()).willReturn("ADMIN");

        Workplace workplace1 = new Workplace();
        workplace1.setId(1L);
        workplace1.setName("회사1");
        workplace1.setInviteCode("CODE123456");

        Workplace workplace2 = new Workplace();
        workplace2.setId(2L);
        workplace2.setName("회사2");
        workplace2.setInviteCode("CODE789ABC");

        given(workplaceRepository.save(any(Workplace.class)))
                .willReturn(workplace1)
                .willReturn(workplace2);

        // When
        Workplace result1 = workPlaceService.createWorkplace(request1);
        Workplace result2 = workPlaceService.createWorkplace(request2);

        // Then
        assertNotNull(result1.getInviteCode());
        assertNotNull(result2.getInviteCode());
    }

    @Test
    @DisplayName("직장 생성 시 이름이 정확히 저장됨")
    void createWorkplace_nameSavedCorrectly() {
        // Given
        String workplaceName = "LG전자";
        WorkPlaceRequest request = new WorkPlaceRequest(workplaceName);

        given(currentUser.getUserRole()).willReturn("ADMIN");

        Workplace expectedWorkplace = new Workplace();
        expectedWorkplace.setId(1L);
        expectedWorkplace.setName(workplaceName);
        expectedWorkplace.setInviteCode("LG12345");

        given(workplaceRepository.save(any(Workplace.class))).willReturn(expectedWorkplace);

        // When
        Workplace result = workPlaceService.createWorkplace(request);

        // Then
        assertEquals(workplaceName, result.getName());
    }

    // ============ 새로운 기능 테스트 코드 ============

    @Test
    @DisplayName("사업장 정보 수정 성공 - ADMIN 권한")
    void updateWorkplace_withAdminRole_success() {
        // Given
        Long userId = 1L;
        WorkPlaceEdit request = new WorkPlaceEdit();
        request.setName("수정된 회사명");
        
        User adminUser = new User();
        adminUser.setId(userId);
        adminUser.setRole(Role.ADMIN);
        
        Workplace workplace = new Workplace();
        workplace.setId(1L);
        workplace.setName("원래 회사명");
        adminUser.setWorkplace(workplace);
        
        given(currentUser.getUserRole()).willReturn("ADMIN");
        given(currentUser.getUserId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(adminUser));
        given(workplaceRepository.save(any(Workplace.class))).willReturn(workplace);
        
        // When
        Workplace result = workPlaceService.updateWorkplace(request);
        
        // Then
        assertNotNull(result);
        assertEquals("수정된 회사명", workplace.getName());
    }

    @Test
    @DisplayName("사업장 정보 수정 실패 - WORKER 권한")
    void updateWorkplace_withWorkerRole_throwsException() {
        // Given
        WorkPlaceEdit request = new WorkPlaceEdit();
        request.setName("수정된 회사명");
        given(currentUser.getUserRole()).willReturn("WORKER");
        
        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> workPlaceService.updateWorkplace(request));
        
        assertEquals("관리자 권한이 필요합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("관리자 권한 부여 성공")
    void assignAdmin_success() {
        // Given
        Long currentUserId = 1L;
        Long newAdminId = 2L;
        
        User currentAdmin = new User();
        currentAdmin.setId(currentUserId);
        currentAdmin.setRole(Role.ADMIN);
        currentAdmin.setName("관리자1");
        
        User newAdmin = new User();
        newAdmin.setId(newAdminId);
        newAdmin.setRole(Role.WORKER);
        newAdmin.setName("관리자2");
        
        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentAdmin));
        given(userRepository.findById(newAdminId)).willReturn(Optional.of(newAdmin));
        given(userRepository.save(any(User.class))).willReturn(newAdmin);
        
        // When
        UserInfo result = workPlaceService.assignAdmin(currentUserId, newAdminId);
        
        // Then
        assertNotNull(result);
        assertEquals(newAdminId, result.getId());
        assertEquals(Role.ADMIN, newAdmin.getRole());
    }

    @Test
    @DisplayName("관리자 권한 부여 실패 - 현재 사용자가 관리자가 아님")
    void assignAdmin_withoutAdminRole_throwsException() {
        // Given
        Long currentUserId = 1L;
        Long newAdminId = 2L;
        
        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setRole(Role.WORKER);
        
        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentUser));
        
        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> workPlaceService.assignAdmin(currentUserId, newAdminId));
        
        assertEquals("관리자 권한이 필요합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("관리자 권한 제거 성공")
    void removeAdmin_success() {
        // Given
        Long currentUserId = 1L;
        Long adminId = 2L;
        
        User currentAdmin = new User();
        currentAdmin.setId(currentUserId);
        currentAdmin.setRole(Role.ADMIN);
        currentAdmin.setName("관리자1");
        
        User adminToRemove = new User();
        adminToRemove.setId(adminId);
        adminToRemove.setRole(Role.ADMIN);
        adminToRemove.setName("관리자2");
        
        given(userRepository.findById(currentUserId)).willReturn(Optional.of(currentAdmin));
        given(userRepository.findById(adminId)).willReturn(Optional.of(adminToRemove));
        given(userRepository.save(any(User.class))).willReturn(adminToRemove);
        
        // When
        UserInfo result = workPlaceService.removeAdmin(currentUserId, adminId);
        
        // Then
        assertNotNull(result);
        assertEquals(adminId, result.getId());
        assertEquals(Role.WORKER, adminToRemove.getRole());
    }

    @Test
    @DisplayName("사업장 탈퇴 성공")
    void leaveWorkplace_success() {
        // Given
        Long userId = 1L;
        
        User worker = new User();
        worker.setId(userId);
        worker.setRole(Role.WORKER);
        worker.setName("근무자1");
        
        Workplace workplace = new Workplace();
        workplace.setId(1L);
        workplace.setName("회사");
        worker.setWorkplace(workplace);
        
        given(userRepository.findById(userId)).willReturn(Optional.of(worker));
        given(userRepository.save(any(User.class))).willReturn(worker);
        
        // When
        UserInfo result = workPlaceService.leaveWorkplace(userId);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    @DisplayName("사업장 탈퇴 실패 - 관리자는 탈퇴 불가")
    void leaveWorkplace_adminCannotLeave_throwsException() {
        // Given
        Long userId = 1L;
        
        User admin = new User();
        admin.setId(userId);
        admin.setRole(Role.ADMIN);
        admin.setName("관리자1");
        
        given(userRepository.findById(userId)).willReturn(Optional.of(admin));
        
        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> workPlaceService.leaveWorkplace(userId));
        
        assertEquals("관리자는 사업장을 탈퇴할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("근무자 강제 퇴장 성공")
    void removeWorker_success() {
        // Given
        Long currentUserId = 1L;
        Long workerId = 2L;
        
        User admin = new User();
        admin.setId(currentUserId);
        admin.setRole(Role.ADMIN);
        
        User worker = new User();
        worker.setId(workerId);
        worker.setRole(Role.WORKER);
        worker.setName("근무자1");
        
        Workplace workplace = new Workplace();
        workplace.setId(1L);
        worker.setWorkplace(workplace);
        
        given(userRepository.findById(currentUserId)).willReturn(Optional.of(admin));
        given(userRepository.findById(workerId)).willReturn(Optional.of(worker));
        given(userRepository.save(any(User.class))).willReturn(worker);
        
        // When
        UserInfo result = workPlaceService.removeWorker(currentUserId, workerId);
        
        // Then
        assertNotNull(result);
        assertEquals(workerId, result.getId());
    }

    @Test
    @DisplayName("근무자 강제 퇴장 실패 - 관리자가 아님")
    void removeWorker_withoutAdminRole_throwsException() {
        // Given
        Long currentUserId = 1L;
        Long workerId = 2L;
        
        User worker = new User();
        worker.setId(currentUserId);
        worker.setRole(Role.WORKER);
        
        given(userRepository.findById(currentUserId)).willReturn(Optional.of(worker));
        
        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> workPlaceService.removeWorker(currentUserId, workerId));
        
        assertEquals("관리자 권한이 필요합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사업장 삭제 성공")
    void deleteWorkplace_success() {
        // Given
        Long userId = 1L;
        
        User admin = new User();
        admin.setId(userId);
        admin.setRole(Role.ADMIN);
        
        Workplace workplace = new Workplace();
        workplace.setId(1L);
        workplace.setName("삭제할 회사");
        workplace.setInviteCode("CODE123");
        workplace.setCreatedAt(LocalDateTime.now());
        admin.setWorkplace(workplace);
        
        User worker = new User();
        worker.setId(2L);
        worker.setRole(Role.WORKER);
        worker.setWorkplace(workplace);
        
        List<User> workplaceUsers = Arrays.asList(admin, worker);
        
        given(currentUser.getUserId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(admin));
        given(userRepository.findByWorkplace(workplace)).willReturn(workplaceUsers);
        given(userRepository.save(any(User.class))).willReturn(new User());
        
        // When
        WorkPlaceInfo result = workPlaceService.deleteWorkplace();
        
        // Then
        assertNotNull(result);
        assertEquals("삭제할 회사", result.getName());
    }

    @Test
    @DisplayName("사업장 삭제 실패 - 관리자가 아님")
    void deleteWorkplace_withoutAdminRole_throwsException() {
        // Given
        Long userId = 1L;
        
        User worker = new User();
        worker.setId(userId);
        worker.setRole(Role.WORKER);
        
        given(currentUser.getUserId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(worker));
        
        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> workPlaceService.deleteWorkplace());
        
        assertEquals("관리자 권한이 필요합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사업장 정보 조회 성공")
    void getWorkplaceInfo_success() {
        // Given
        Long userId = 1L;
        
        User user = new User();
        user.setId(userId);
        user.setRole(Role.ADMIN);
        user.setName("관리자");
        
        Workplace workplace = new Workplace();
        workplace.setId(1L);
        workplace.setName("회사");
        workplace.setInviteCode("CODE123");
        workplace.setCreatedAt(LocalDateTime.now());
        user.setWorkplace(workplace);
        
        User admin = new User();
        admin.setId(userId);
        admin.setRole(Role.ADMIN);
        admin.setName("관리자");
        
        User worker = new User();
        worker.setId(2L);
        worker.setRole(Role.WORKER);
        worker.setName("근무자");
        
        List<User> workplaceUsers = Arrays.asList(admin, worker);
        
        given(currentUser.getUserId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.findByWorkplace(workplace)).willReturn(workplaceUsers);
        
        // When
        WorkPlaceInfo result = workPlaceService.getWorkplaceInfo();
        
        // Then
        assertNotNull(result);
        assertEquals("회사", result.getName());
        assertEquals(1, result.getAdmins().size());
        assertEquals(1, result.getUsers().size());
    }
}
