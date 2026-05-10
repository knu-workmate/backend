package com.workmate.workmate.work.service;

import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.work.dto.*;
import com.workmate.workmate.work.entity.Schedule;
import com.workmate.workmate.work.entity.Substitute;
import com.workmate.workmate.work.entity.SubstituteStatus;
import com.workmate.workmate.work.repository.ScheduleRepository;
import com.workmate.workmate.work.repository.SubstituteRepository;
import com.workmate.workmate.work.repository.SubstituteHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService 테스트")
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubstituteRepository substituteRepository;

    @Mock
    private SubstituteHistoryRepository substituteHistoryRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private User testUser;
    private User testAdmin;
    private Workplace testWorkplace;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        // Workplace 설정
        testWorkplace = new Workplace();
        testWorkplace.setId(1L);
        testWorkplace.setName("테스트 카페");
        testWorkplace.setDeleted(false);

        // 일반 사용자 설정
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setName("일반 사용자");
        testUser.setRole(Role.WORKER);
        testUser.setWorkplace(testWorkplace);
        testUser.setDeleted(false);

        // 관리자 설정
        testAdmin = new User();
        testAdmin.setId(2L);
        testAdmin.setEmail("admin@test.com");
        testAdmin.setName("관리자");
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setWorkplace(testWorkplace);
        testAdmin.setDeleted(false);

        // 스케줄 설정
        testSchedule = new Schedule();
        testSchedule.setId(1L);
        testSchedule.setUser(testUser);
        testSchedule.setWorkplace(testWorkplace);
        testSchedule.setWorkDate(LocalDate.of(2024, 7, 1));
        testSchedule.setStartTime(LocalTime.of(9, 0));
        testSchedule.setEndTime(LocalTime.of(18, 0));
        testSchedule.setNote("테스트 근무");
    }

    // ===== saveSchedule =====
    @Test
    @DisplayName("saveSchedule - 성공")
    void testSaveSchedule_Success() {
        // arrange
        ScheduleRequest request = ScheduleRequest.builder()
                .workDate(LocalDate.of(2024, 7, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .note("테스트 근무")
                .build();

        given(scheduleRepository.findByUserIdAndWorkDate(testUser.getId(), request.getWorkDate()))
                .willReturn(new ArrayList<>());
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(scheduleRepository.save(any(Schedule.class))).willReturn(testSchedule);

        // act
        List<ScheduleResponse> result = scheduleService.saveSchedule(List.of(request), testUser.getId());

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    @Test
    @DisplayName("saveSchedule - 시간 충돌")
    void testSaveSchedule_TimeConflict() {
        // arrange
        ScheduleRequest request = ScheduleRequest.builder()
                .workDate(LocalDate.of(2024, 7, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        Schedule existingSchedule = new Schedule();
        existingSchedule.setStartTime(LocalTime.of(10, 0));
        existingSchedule.setEndTime(LocalTime.of(15, 0));

        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(scheduleRepository.findByUserIdAndWorkDate(testUser.getId(), request.getWorkDate()))
                .willReturn(List.of(existingSchedule));

        // act & assert
        assertThrows(IllegalArgumentException.class,
            () -> scheduleService.saveSchedule(List.of(request), testUser.getId()));
    }

    // ===== saveScheduleAdmin =====
    @Test
    @DisplayName("saveScheduleAdmin - 성공")
    void testSaveScheduleAdmin_Success() {
        // arrange
        ScheduleRequest request = ScheduleRequest.builder()
                .workDate(LocalDate.of(2024, 7, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        given(userRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(scheduleRepository.findByUserIdAndWorkDate(testUser.getId(), request.getWorkDate()))
                .willReturn(new ArrayList<>());
        given(scheduleRepository.save(any(Schedule.class))).willReturn(testSchedule);

        // act
        List<ScheduleResponse> result = scheduleService.saveScheduleAdmin(List.of(request), testUser.getId(), testAdmin.getId());

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    @Test
    @DisplayName("saveScheduleAdmin - 관리자 권한 없음")
    void testSaveScheduleAdmin_NotAdmin() {
        // arrange
        ScheduleRequest request = ScheduleRequest.builder()
                .workDate(LocalDate.of(2024, 7, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        User normalUser = new User();
        normalUser.setId(3L);
        normalUser.setRole(Role.WORKER);
        normalUser.setWorkplace(testWorkplace);

        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(userRepository.findById(normalUser.getId())).willReturn(Optional.of(normalUser));

        // act & assert
        assertThrows(IllegalArgumentException.class,
            () -> scheduleService.saveScheduleAdmin(List.of(request), testUser.getId(), normalUser.getId()));
    }

    // ===== deleteSchedule =====
    @Test
    @DisplayName("deleteSchedule - 성공")
    void testDeleteSchedule_Success() {
        // arrange
        List<Long> scheduleIds = List.of(testSchedule.getId());
        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));

        // act
        List<ScheduleResponse> result = scheduleService.deleteSchedule(scheduleIds, testUser.getId());

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleRepository, times(1)).delete(testSchedule);
    }

    @Test
    @DisplayName("deleteSchedule - 스케줄 미발견")
    void testDeleteSchedule_ScheduleNotFound() {
        // arrange
        List<Long> scheduleIds = List.of(999L);
        given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

        // act & assert
        assertThrows(IllegalArgumentException.class,
            () -> scheduleService.deleteSchedule(scheduleIds, testUser.getId()));
    }

    @Test
    @DisplayName("deleteSchedule - 소유자 아님")
    void testDeleteSchedule_NotOwner() {
        // arrange
        List<Long> scheduleIds = List.of(testSchedule.getId());
        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));

        // act & assert
        assertThrows(IllegalArgumentException.class,
            () -> scheduleService.deleteSchedule(scheduleIds, 999L));
    }

    @Test
    @DisplayName("deleteSchedule - 스케줄 삭제 (대타는 엔티티 수준에서 자동 삭제)")
    void testDeleteSchedule_WithSubstitutes() {
        // arrange
        List<Long> scheduleIds = List.of(testSchedule.getId());

        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));

        // act
        List<ScheduleResponse> result = scheduleService.deleteSchedule(scheduleIds, testUser.getId());

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleRepository, times(1)).delete(testSchedule);
    }

    // ===== deleteScheduleAdmin =====
    @Test
    @DisplayName("deleteScheduleAdmin - 성공")
    void testDeleteScheduleAdmin_Success() {
        // arrange
        List<Long> scheduleIds = List.of(testSchedule.getId());
        given(userRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));
        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));

        // act
        List<ScheduleResponse> result = scheduleService.deleteScheduleAdmin(scheduleIds, testAdmin.getId());

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scheduleRepository, times(1)).delete(testSchedule);
    }

    @Test
    @DisplayName("deleteScheduleAdmin - 관리자 권한 없음")
    void testDeleteScheduleAdmin_NotAdmin() {
        // arrange
        List<Long> scheduleIds = List.of(testSchedule.getId());
        User normalUser = new User();
        normalUser.setId(3L);
        normalUser.setRole(Role.WORKER);

        given(userRepository.findById(normalUser.getId())).willReturn(Optional.of(normalUser));

        // act & assert
        assertThrows(IllegalArgumentException.class,
            () -> scheduleService.deleteScheduleAdmin(scheduleIds, normalUser.getId()));
    }

    @Test
    @DisplayName("deleteScheduleAdmin - 다른 사업장 스케줄")
    void testDeleteScheduleAdmin_DifferentWorkplace() {
        // arrange
        List<Long> scheduleIds = List.of(testSchedule.getId());

        Workplace otherWorkplace = new Workplace();
        otherWorkplace.setId(2L);
        otherWorkplace.setName("다른 카페");

        User otherAdmin = new User();
        otherAdmin.setId(4L);
        otherAdmin.setEmail("other-admin@test.com");
        otherAdmin.setName("다른 관리자");
        otherAdmin.setRole(Role.ADMIN);
        otherAdmin.setWorkplace(otherWorkplace);

        testSchedule.setWorkplace(testWorkplace);  // 원래 근무지

        given(userRepository.findById(otherAdmin.getId())).willReturn(Optional.of(otherAdmin));
        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));

        // act & assert
        assertThrows(IllegalArgumentException.class,
            () -> scheduleService.deleteScheduleAdmin(scheduleIds, otherAdmin.getId()));
    }

    // ===== updateSchedule =====
    @Test
    @DisplayName("updateSchedule - 성공")
    void testUpdateSchedule_Success() {
        // arrange
        SchedulePatchRequest patchRequest = new SchedulePatchRequest();
        patchRequest.setScheduleId(testSchedule.getId());
        patchRequest.setNewStartTime(LocalTime.of(10, 0));
        patchRequest.setNewEndTime(LocalTime.of(19, 0));
        patchRequest.setNewNote("수정된 근무");

        Schedule updatedSchedule = new Schedule();
        updatedSchedule.setId(testSchedule.getId());
        updatedSchedule.setUser(testUser);
        updatedSchedule.setWorkplace(testWorkplace);
        updatedSchedule.setWorkDate(testSchedule.getWorkDate());
        updatedSchedule.setStartTime(LocalTime.of(10, 0));
        updatedSchedule.setEndTime(LocalTime.of(19, 0));
        updatedSchedule.setNote("수정된 근무");

        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));
        given(scheduleRepository.save(any(Schedule.class))).willReturn(updatedSchedule);

        // act
        ScheduleResponse result = scheduleService.updateSchedule(patchRequest, testUser.getId());

        // assert
        assertNotNull(result);
        assertEquals(LocalTime.of(10, 0), result.getStartTime());
        assertEquals(LocalTime.of(19, 0), result.getEndTime());
        assertEquals("수정된 근무", result.getNote());
    }

    @Test
    @DisplayName("updateSchedule - 스케줄 미발견")
    void testUpdateSchedule_ScheduleNotFound() {
        // arrange
        SchedulePatchRequest patchRequest = new SchedulePatchRequest();
        patchRequest.setScheduleId(999L);

        given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

        // act & assert
        assertThrows(IllegalArgumentException.class,
            () -> scheduleService.updateSchedule(patchRequest, testUser.getId()));
    }

    // ===== updateScheduleAdmin =====
    @Test
    @DisplayName("updateScheduleAdmin - 성공")
    void testUpdateScheduleAdmin_Success() {
        // arrange
        SchedulePatchRequest patchRequest = new SchedulePatchRequest();
        patchRequest.setScheduleId(testSchedule.getId());
        patchRequest.setNewStartTime(LocalTime.of(10, 0));
        patchRequest.setNewEndTime(LocalTime.of(19, 0));
        patchRequest.setNewNote("관리자 수정");

        Schedule updatedSchedule = new Schedule();
        updatedSchedule.setId(testSchedule.getId());
        updatedSchedule.setUser(testUser);
        updatedSchedule.setWorkplace(testWorkplace);
        updatedSchedule.setWorkDate(testSchedule.getWorkDate());
        updatedSchedule.setStartTime(LocalTime.of(10, 0));
        updatedSchedule.setEndTime(LocalTime.of(19, 0));
        updatedSchedule.setNote("관리자 수정");

        given(userRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));
        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));
        given(scheduleRepository.save(any(Schedule.class))).willReturn(updatedSchedule);

        // act
        ScheduleResponse result = scheduleService.updateScheduleAdmin(patchRequest, testAdmin.getId());

        // assert
        assertNotNull(result);
        assertEquals("관리자 수정", result.getNote());
    }

    @Test
    @DisplayName("updateScheduleAdmin - 관리자 권한 없음")
    void testUpdateScheduleAdmin_NotAdmin() {
        // arrange
        SchedulePatchRequest patchRequest = new SchedulePatchRequest();
        patchRequest.setScheduleId(testSchedule.getId());

        User normalUser = new User();
        normalUser.setId(3L);
        normalUser.setRole(Role.WORKER);

        given(userRepository.findById(normalUser.getId())).willReturn(Optional.of(normalUser));

        // act & assert
        assertThrows(IllegalArgumentException.class,
            () -> scheduleService.updateScheduleAdmin(patchRequest, normalUser.getId()));
    }

    // ===== 삭제된 사용자/사업장 검증 테스트 =====

    @Test
    @DisplayName("saveSchedule - 삭제된 사용자")
    void testSaveSchedule_DeletedUser() {
        // arrange
        User deletedUser = new User();
        deletedUser.setId(5L);
        deletedUser.setRole(Role.WORKER);
        deletedUser.setDeleted(true);
        deletedUser.setWorkplace(testWorkplace);

        ScheduleRequest request = ScheduleRequest.builder()
                .workDate(LocalDate.of(2024, 7, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        given(userRepository.findById(deletedUser.getId())).willReturn(Optional.of(deletedUser));

        // act & assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> scheduleService.saveSchedule(List.of(request), deletedUser.getId()));

        assertEquals("삭제된 사용자입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("saveSchedule - 삭제된 사업장")
    void testSaveSchedule_DeletedWorkplace() {
        // arrange
        Workplace deletedWorkplace = new Workplace();
        deletedWorkplace.setId(2L);
        deletedWorkplace.setDeleted(true);

        User userWithDeletedWorkplace = new User();
        userWithDeletedWorkplace.setId(6L);
        userWithDeletedWorkplace.setRole(Role.WORKER);
        userWithDeletedWorkplace.setDeleted(false);
        userWithDeletedWorkplace.setWorkplace(deletedWorkplace);

        ScheduleRequest request = ScheduleRequest.builder()
                .workDate(LocalDate.of(2024, 7, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        given(userRepository.findById(userWithDeletedWorkplace.getId())).willReturn(Optional.of(userWithDeletedWorkplace));

        // act & assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> scheduleService.saveSchedule(List.of(request), userWithDeletedWorkplace.getId()));

        assertEquals("삭제된 근무지입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("saveScheduleAdmin - 삭제된 대상 사용자")
    void testSaveScheduleAdmin_DeletedTargetUser() {
        // arrange
        User deletedTargetUser = new User();
        deletedTargetUser.setId(7L);
        deletedTargetUser.setRole(Role.WORKER);
        deletedTargetUser.setDeleted(true);
        deletedTargetUser.setWorkplace(testWorkplace);

        ScheduleRequest request = ScheduleRequest.builder()
                .workDate(LocalDate.of(2024, 7, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        given(userRepository.findById(deletedTargetUser.getId())).willReturn(Optional.of(deletedTargetUser));
        given(userRepository.findById(testAdmin.getId())).willReturn(Optional.of(testAdmin));

        // act & assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> scheduleService.saveScheduleAdmin(List.of(request), deletedTargetUser.getId(), testAdmin.getId()));

        assertEquals("대상 사용자가 삭제되었습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("saveScheduleAdmin - 삭제된 관리자 사용자")
    void testSaveScheduleAdmin_DeletedAdminUser() {
        // arrange
        User deletedAdmin = new User();
        deletedAdmin.setId(8L);
        deletedAdmin.setRole(Role.ADMIN);
        deletedAdmin.setDeleted(true);
        deletedAdmin.setWorkplace(testWorkplace);

        ScheduleRequest request = ScheduleRequest.builder()
                .workDate(LocalDate.of(2024, 7, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(userRepository.findById(deletedAdmin.getId())).willReturn(Optional.of(deletedAdmin));

        // act & assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> scheduleService.saveScheduleAdmin(List.of(request), testUser.getId(), deletedAdmin.getId()));

        assertEquals("관리자 사용자가 삭제되었습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("getScheduleDay - 삭제된 사용자의 스케줄 조회")
    void testGetScheduleDay_DeletedUser() {
        // arrange
        User deletedUser = new User();
        deletedUser.setId(9L);
        deletedUser.setName("삭제된 사용자");
        deletedUser.setRole(Role.WORKER);
        deletedUser.setDeleted(true);
        deletedUser.setWorkplace(testWorkplace);

        Schedule scheduleByDeletedUser = new Schedule();
        scheduleByDeletedUser.setId(2L);
        scheduleByDeletedUser.setUser(deletedUser);
        scheduleByDeletedUser.setWorkplace(testWorkplace);
        scheduleByDeletedUser.setWorkDate(LocalDate.of(2024, 7, 1));
        scheduleByDeletedUser.setStartTime(LocalTime.of(9, 0));
        scheduleByDeletedUser.setEndTime(LocalTime.of(18, 0));
        scheduleByDeletedUser.setNote("테스트 근무");

        given(userRepository.findById(deletedUser.getId())).willReturn(Optional.of(deletedUser));
        given(scheduleRepository.findByUserAndWorkDate(deletedUser, LocalDate.of(2024, 7, 1)))
                .willReturn(List.of(scheduleByDeletedUser));

        // act
        List<ScheduleResponse> result = scheduleService.getScheduleDay(deletedUser.getId(), LocalDate.of(2024, 7, 1));

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getUserName().contains("(탈퇴한 사용자)"));
        assertTrue(result.get(0).getNote().contains("(사용자 탈퇴로 인해 스케줄 정보가 정확하지 않을 수 있습니다.)"));
    }
}
