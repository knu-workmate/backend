package com.workmate.workmate.work.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import com.workmate.workmate.work.entity.Schedule;
import com.workmate.workmate.work.entity.Substitute;
import com.workmate.workmate.work.entity.SubstituteStatus;

@SpringBootTest(classes = WorkmateApplication.class)
@Transactional
@DisplayName("Substitute Repository 테스트")
public class SubstituteRepositoryTest {

    @Autowired
    private SubstituteRepository substituteRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkplaceRepository workplaceRepository;

    private User requester;
    private User substituteUser;
    private User otherUser;
    private Workplace workplace;
    private Schedule schedule;
    private Substitute substituteA;
    private Substitute substituteB;
    private Substitute substituteC;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        requester = new User();
        requester.setName("요청자");
        requester.setEmail("requester@example.com");
        requester.setPassword("password");
        requester.setRole(Role.WORKER);
        requester = userRepository.save(requester);

        substituteUser = new User();
        substituteUser.setName("대타자");
        substituteUser.setEmail("substitute@example.com");
        substituteUser.setPassword("password");
        substituteUser.setRole(Role.WORKER);
        substituteUser = userRepository.save(substituteUser);

        otherUser = new User();
        otherUser.setName("다른 사용자");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole(Role.WORKER);
        otherUser = userRepository.save(otherUser);

        // 워크플레이스 생성
        workplace = new Workplace();
        workplace.setName("테스트 워크플레이스");
        workplace.setInviteCode("TEST123");
        workplace = workplaceRepository.save(workplace);

        // 스케줄 생성
        schedule = new Schedule();
        schedule.setWorkDate(LocalDate.of(2024, 4, 6));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));
        schedule.setUser(requester);
        schedule.setWorkplace(workplace);
        schedule.setNote("테스트 근무");
        schedule = scheduleRepository.save(schedule);

        // 대타 요청 생성
        substituteA = new Substitute();
        substituteA.setSchedule(schedule);
        substituteA.setRequester(requester);
        substituteA.setSubstituteUser(substituteUser);
        substituteA.setStatus(SubstituteStatus.REQUESTED);

        substituteB = new Substitute();
        substituteB.setSchedule(schedule);
        substituteB.setRequester(requester);
        substituteB.setSubstituteUser(null); // 대타자가 아직 승인되지 않은 경우
        substituteB.setStatus(SubstituteStatus.APPROVED);

        substituteC = new Substitute();
        substituteC.setSchedule(schedule);
        substituteC.setRequester(otherUser);
        substituteC.setSubstituteUser(substituteUser);
        substituteC.setStatus(SubstituteStatus.REJECTED);
    }

    @Test
    @DisplayName("Substitute 저장 및 ID 조회 성공")
    void testSaveAndFindById() {
        Substitute saved = substituteRepository.save(substituteA);

        Optional<Substitute> found = substituteRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(substituteA.getSchedule().getId(), found.get().getSchedule().getId());
        assertEquals(requester.getId(), found.get().getRequester().getId());
        assertEquals(substituteUser.getId(), found.get().getSubstituteUser().getId());
        assertEquals(SubstituteStatus.REQUESTED, found.get().getStatus());
    }

    @Test
    @DisplayName("Requester ID로 Substitute 목록 조회 성공")
    void testFindByRequesterIdSuccess() {
        substituteRepository.save(substituteA);
        substituteRepository.save(substituteB);

        List<Substitute> found = substituteRepository.findByRequester_Id(requester.getId());

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(s -> requester.getId().equals(s.getRequester().getId())));
    }

    @Test
    @DisplayName("SubstituteUser ID로 Substitute 목록 조회 성공")
    void testFindBySubstituteUserIdSuccess() {
        substituteRepository.save(substituteA);
        substituteRepository.save(substituteC);

        List<Substitute> found = substituteRepository.findBySubstituteUser_Id(substituteUser.getId());

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(s -> substituteUser.getId().equals(s.getSubstituteUser().getId())));
    }

    @Test
    @DisplayName("Requester ID와 Status로 Substitute 목록 조회 성공")
    void testFindByRequesterIdAndStatusSuccess() {
        substituteRepository.save(substituteA);
        substituteRepository.save(substituteB);

        List<Substitute> requested = substituteRepository.findByRequester_IdAndStatus(requester.getId(), SubstituteStatus.REQUESTED);
        List<Substitute> approved = substituteRepository.findByRequester_IdAndStatus(requester.getId(), SubstituteStatus.APPROVED);

        assertEquals(1, requested.size());
        assertEquals(SubstituteStatus.REQUESTED, requested.get(0).getStatus());

        assertEquals(1, approved.size());
        assertEquals(SubstituteStatus.APPROVED, approved.get(0).getStatus());
    }

    @Test
    @DisplayName("Substitute 업데이트 성공")
    void testUpdateSubstitute() {
        Substitute saved = substituteRepository.save(substituteA);
        saved.setStatus(SubstituteStatus.APPROVED);
        saved.setSubstituteUser(substituteUser);
        substituteRepository.save(saved);

        Optional<Substitute> found = substituteRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(SubstituteStatus.APPROVED, found.get().getStatus());
        assertEquals(substituteUser.getId(), found.get().getSubstituteUser().getId());
    }

    @Test
    @DisplayName("Substitute 삭제 성공")
    void testDeleteSubstitute() {
        Substitute saved = substituteRepository.save(substituteA);

        substituteRepository.deleteById(saved.getId());

        assertTrue(substituteRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("Substitute 수 조회 성공")
    void testCountSubstitutes() {
        substituteRepository.save(substituteA);
        substituteRepository.save(substituteB);
        substituteRepository.save(substituteC);

        assertEquals(3, substituteRepository.count());
    }

    @Test
    @DisplayName("빈 Substitute 목록 조회")
    void testFindEmptySubstitutes() {
        List<Substitute> found = substituteRepository.findByRequester_Id(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("SubstituteUser가 null인 경우 조회 성공")
    void testFindSubstituteWithNullSubstituteUser() {
        substituteRepository.save(substituteB);

        List<Substitute> found = substituteRepository.findByRequester_Id(requester.getId());

        assertEquals(1, found.size());
        assertNull(found.get(0).getSubstituteUser());
        assertEquals(SubstituteStatus.APPROVED, found.get(0).getStatus());
    }
}