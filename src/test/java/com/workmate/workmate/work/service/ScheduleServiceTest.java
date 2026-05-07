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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubstituteRepository substituteRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private User testUser;
    private Workplace testWorkplace;
    private Schedule testSchedule;
    private ScheduleRequest scheduleRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 Workplace 설정
        testWorkplace = new Workplace();
        testWorkplace.setId(1L);
        testWorkplace.setName("카페");

        // 테스트용 User 설정
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setName("김철수");
        testUser.setPassword("password123");
        testUser.setRole(Role.WORKER);
        testUser.setWorkplace(testWorkplace);

        // 테스트용 Schedule 설정
        testSchedule = new Schedule();
        testSchedule.setId(1L);
        testSchedule.setUser(testUser);
        testSchedule.setWorkplace(testWorkplace);
        testSchedule.setWorkDate(LocalDate.of(2024, 7, 1));
        testSchedule.setStartTime(LocalTime.of(9, 0));
        testSchedule.setEndTime(LocalTime.of(18, 0));
        testSchedule.setNote("일반 근무");

        // 테스트용 ScheduleRequest 설정
        scheduleRequest = ScheduleRequest.builder()
                .workDate(LocalDate.of(2024, 7, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .note("일반 근무")
                .build();
    }

    @Test
    void saveSchedule_validRequest_returnsSavedSchedule() {
        // arrange
        List<ScheduleRequest> requests = Arrays.asList(scheduleRequest);
        given(scheduleRepository.findByUserIdAndWorkDate(testUser.getId(), scheduleRequest.getWorkDate()))
                .willReturn(new ArrayList<>());
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        
        // save 호출 시 입력 Schedule 객체에 ID를 설정
        doAnswer(new Answer<Schedule>() {
            @Override
            public Schedule answer(InvocationOnMock invocation) throws Throwable {
                Schedule schedule = invocation.getArgument(0);
                schedule.setId(1L);
                return schedule;
            }
        }).when(scheduleRepository).save(any(Schedule.class));

        // act
        List<ScheduleResponse> responses = scheduleService.saveSchedule(requests, testUser.getId());

        // assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("김철수", responses.get(0).getUserName());
        assertEquals("카페", responses.get(0).getWorkplaceName());
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    @Test
    void saveSchedule_timeConflict_throwsIllegalArgumentException() {
        // arrange
        List<ScheduleRequest> requests = Arrays.asList(scheduleRequest);
        Schedule conflictingSchedule = new Schedule();
        conflictingSchedule.setStartTime(LocalTime.of(8, 0));
        conflictingSchedule.setEndTime(LocalTime.of(10, 0));
        
        given(scheduleRepository.findByUserIdAndWorkDate(testUser.getId(), scheduleRequest.getWorkDate()))
                .willReturn(Arrays.asList(conflictingSchedule));

        // act & assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.saveSchedule(requests, testUser.getId()));
        assertEquals("해당 시간에 이미 스케줄이 존재합니다.", exception.getMessage());
    }

    @Test
    void saveSchedule_userNotFound_throwsIllegalArgumentException() {
        // arrange
        List<ScheduleRequest> requests = Arrays.asList(scheduleRequest);
        given(scheduleRepository.findByUserIdAndWorkDate(testUser.getId(), scheduleRequest.getWorkDate()))
                .willReturn(new ArrayList<>());
        given(userRepository.findById(testUser.getId())).willReturn(Optional.empty());

        // act & assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.saveSchedule(requests, testUser.getId()));
        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void saveScheduleAdmin_validAdminAndUser_returnsSavedSchedule() {
        // arrange
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setName("관리자");
        adminUser.setRole(Role.ADMIN);
        adminUser.setWorkplace(testWorkplace);

        List<ScheduleRequest> requests = Arrays.asList(scheduleRequest);
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(userRepository.findById(adminUser.getId())).willReturn(Optional.of(adminUser));
        given(scheduleRepository.findByUserIdAndWorkDate(testUser.getId(), scheduleRequest.getWorkDate()))
                .willReturn(new ArrayList<>());
        given(scheduleRepository.save(any(Schedule.class))).willReturn(testSchedule);

        // act
        List<ScheduleResponse> responses = scheduleService.saveScheduleAdmin(requests, testUser.getId(), adminUser.getId());

        // assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void saveScheduleAdmin_adminHasNoDifferentWorkplace_throwsIllegalArgumentException() {
        // arrange
        User differentAdminUser = new User();
        differentAdminUser.setId(2L);
        differentAdminUser.setEmail("different-admin@example.com");
        differentAdminUser.setName("다른근무지관리자");
        differentAdminUser.setRole(Role.ADMIN);
        Workplace differentWorkplace = new Workplace();
        differentWorkplace.setId(2L);
        differentWorkplace.setName("다른 카페");
        differentAdminUser.setWorkplace(differentWorkplace);

        List<ScheduleRequest> requests = Arrays.asList(scheduleRequest);
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(userRepository.findById(differentAdminUser.getId())).willReturn(Optional.of(differentAdminUser));

        // act & assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.saveScheduleAdmin(requests, testUser.getId(), differentAdminUser.getId()));
        assertEquals("관리자 권한이 없습니다.", exception.getMessage());
    }

    @Test
    void saveScheduleAdmin_userIsNotAdmin_throwsIllegalArgumentException() {
        // arrange
        User nonAdminUser = new User();
        nonAdminUser.setId(2L);
        nonAdminUser.setEmail("user2@example.com");
        nonAdminUser.setName("일반사용자");
        nonAdminUser.setRole(Role.WORKER);
        nonAdminUser.setWorkplace(testWorkplace);

        List<ScheduleRequest> requests = Arrays.asList(scheduleRequest);
        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(userRepository.findById(nonAdminUser.getId())).willReturn(Optional.of(nonAdminUser));

        // act & assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.saveScheduleAdmin(requests, testUser.getId(), nonAdminUser.getId()));
        assertEquals("관리자 권한이 없습니다.", exception.getMessage());
    }

    @Test
    void deleteSchedule_validSchedule_returnsDeletedSchedule() {
        // arrange
        List<Long> scheduleIds = Arrays.asList(testSchedule.getId());
        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));

        // act
        List<ScheduleResponse> responses = scheduleService.deleteSchedule(scheduleIds, testUser.getId());

        // assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testSchedule.getId(), responses.get(0).getId());
        verify(scheduleRepository, times(1)).delete(testSchedule);
    }

    @Test
    void deleteSchedule_scheduleNotFound_throwsIllegalArgumentException() {
        // arrange
        List<Long> scheduleIds = Arrays.asList(999L);
        given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

        // act & assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.deleteSchedule(scheduleIds, testUser.getId()));
        assertEquals("스케줄을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void deleteSchedule_userNotOwner_throwsIllegalArgumentException() {
        // arrange
        List<Long> scheduleIds = Arrays.asList(testSchedule.getId());
        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));

        // act & assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.deleteSchedule(scheduleIds, 999L));
        assertEquals("사용자에게 해당 스케줄이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void updateSchedule_validRequest_returnsUpdatedSchedule() {
        // arrange
        SchedulePatchRequest patchRequest = new SchedulePatchRequest();
        patchRequest.setScheduleId(testSchedule.getId());
        patchRequest.setNewStartTime(LocalTime.of(10, 0));
        patchRequest.setNewEndTime(LocalTime.of(19, 0));
        patchRequest.setNewNote("시간변경 근무");

        Schedule updatedSchedule = new Schedule();
        updatedSchedule.setId(testSchedule.getId());
        updatedSchedule.setUser(testUser);
        updatedSchedule.setWorkplace(testWorkplace);
        updatedSchedule.setWorkDate(testSchedule.getWorkDate());
        updatedSchedule.setStartTime(LocalTime.of(10, 0));
        updatedSchedule.setEndTime(LocalTime.of(19, 0));
        updatedSchedule.setNote("시간변경 근무");

        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));
        given(scheduleRepository.save(any(Schedule.class))).willReturn(updatedSchedule);

        // act
        ScheduleResponse response = scheduleService.updateSchedule(patchRequest, testUser.getId());

        // assert
        assertNotNull(response);
        assertEquals(LocalTime.of(10, 0), response.getStartTime());
        assertEquals(LocalTime.of(19, 0), response.getEndTime());
        assertEquals("시간변경 근무", response.getNote());
    }

    @Test
    void updateSchedule_scheduleNotFound_throwsIllegalArgumentException() {
        // arrange
        SchedulePatchRequest patchRequest = new SchedulePatchRequest();
        patchRequest.setScheduleId(999L);
        given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

        // act & assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.updateSchedule(patchRequest, testUser.getId()));
        assertEquals("스케줄을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void updateSchedule_userNotOwner_throwsIllegalArgumentException() {
        // arrange
        SchedulePatchRequest patchRequest = new SchedulePatchRequest();
        patchRequest.setScheduleId(testSchedule.getId());
        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));

        // act & assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scheduleService.updateSchedule(patchRequest, 999L));
        assertEquals("사용자에게 해당 스케줄이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void getScheduleByDateRange_validDateRange_returnsSchedules() {
        // arrange
        LocalDate startDate = LocalDate.of(2024, 7, 1);
        LocalDate endDate = LocalDate.of(2024, 7, 31);
        List<Schedule> schedules = Arrays.asList(testSchedule);

        given(scheduleRepository.findByUser_Id(testUser.getId())).willReturn(schedules);
        given(substituteRepository.findBySchedule_IdAndStatus(testSchedule.getId(), SubstituteStatus.APPROVED))
                .willReturn(Optional.empty());
        given(substituteRepository.findBySubstituteUser_IdAndStatus(testUser.getId(), SubstituteStatus.APPROVED))
                .willReturn(new ArrayList<>());

        // act
        ScheduleGetResponse response = scheduleService.getScheduleByDateRange(testUser.getId(), startDate, endDate);

        // assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals("김철수", response.getUserName());
        assertEquals("카페", response.getWorkplaceName());
        assertNotNull(response.getScheduleDates());
    }

    @Test
    void getScheduleByDateRange_emptySchedule_returnsEmptyResponse() {
        // arrange
        LocalDate startDate = LocalDate.of(2024, 7, 1);
        LocalDate endDate = LocalDate.of(2024, 7, 31);
        given(scheduleRepository.findByUser_Id(testUser.getId())).willReturn(new ArrayList<>());

        // act
        ScheduleGetResponse response = scheduleService.getScheduleByDateRange(testUser.getId(), startDate, endDate);

        // assert
        assertNotNull(response);
        assertTrue(response.getScheduleDates() == null || response.getScheduleDates().isEmpty());
    }

    @Test
    void getEntireSchedule_validDateRange_returnsAllWorkerSchedules() {
        // arrange
        LocalDate startDate = LocalDate.of(2024, 7, 1);
        LocalDate endDate = LocalDate.of(2024, 7, 2);
        List<Schedule> schedules = Arrays.asList(testSchedule);

        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(scheduleRepository.findByWorkplace_IdAndWorkDate(testWorkplace.getId(), startDate))
                .willReturn(schedules);
        given(scheduleRepository.findByWorkplace_IdAndWorkDate(testWorkplace.getId(), endDate))
                .willReturn(new ArrayList<>());
        given(substituteRepository.findBySchedule_IdAndStatus(testSchedule.getId(), SubstituteStatus.APPROVED))
                .willReturn(Optional.empty());

        // act
        List<EntireScheduleResponse> responses = scheduleService.getEntireSchedule(testUser.getId(), startDate, endDate);

        // assert
        assertNotNull(responses);
        assertTrue(responses.size() > 0);
    }

    @Test
    void getEntireSchedule_noScheduleInPeriod_returnsEmptyList() {
        // arrange
        LocalDate startDate = LocalDate.of(2024, 7, 1);
        LocalDate endDate = LocalDate.of(2024, 7, 2);

        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(scheduleRepository.findByWorkplace_IdAndWorkDate(testWorkplace.getId(), startDate))
                .willReturn(new ArrayList<>());
        given(scheduleRepository.findByWorkplace_IdAndWorkDate(testWorkplace.getId(), endDate))
                .willReturn(new ArrayList<>());

        // act
        List<EntireScheduleResponse> responses = scheduleService.getEntireSchedule(testUser.getId(), startDate, endDate);

        // assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void deleteScheduleAdmin_validAdminAndSchedule_returnsDeletedSchedule() {
        // arrange
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setName("관리자");
        adminUser.setRole(Role.ADMIN);
        adminUser.setWorkplace(testWorkplace);

        List<Long> scheduleIds = Arrays.asList(testSchedule.getId());
        given(scheduleRepository.findById(testSchedule.getId())).willReturn(Optional.of(testSchedule));
        given(userRepository.findById(adminUser.getId())).willReturn(Optional.of(adminUser));

        // act
        List<ScheduleResponse> responses = scheduleService.deleteScheduleAdmin(scheduleIds, adminUser.getId());

        // assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(scheduleRepository, times(1)).delete(testSchedule);
    }
}
