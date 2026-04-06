package com.workmate.workmate.work.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

@SpringBootTest(classes = WorkmateApplication.class)
@Transactional
@DisplayName("Schedule Repository 테스트")
public class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkplaceRepository workplaceRepository;

    private User userA;
    private User userB;
    private Workplace workplaceA;
    private Workplace workplaceB;
    private Schedule scheduleA;
    private Schedule scheduleB;
    private Schedule scheduleC;

    @BeforeEach
    void setUp() {
        // 사용자 생성
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

        // 워크플레이스 생성
        workplaceA = new Workplace();
        workplaceA.setName("워크플레이스 A");
        workplaceA.setInviteCode("INVITEA");
        workplaceA = workplaceRepository.save(workplaceA);

        workplaceB = new Workplace();
        workplaceB.setName("워크플레이스 B");
        workplaceB.setInviteCode("INVITEB");
        workplaceB = workplaceRepository.save(workplaceB);

        // 스케줄 생성
        scheduleA = new Schedule();
        scheduleA.setWorkDate(LocalDate.of(2024, 4, 6));
        scheduleA.setStartTime(LocalTime.of(9, 0));
        scheduleA.setEndTime(LocalTime.of(17, 0));
        scheduleA.setUser(userA);
        scheduleA.setWorkplace(workplaceA);
        scheduleA.setNote("아침 근무");

        scheduleB = new Schedule();
        scheduleB.setWorkDate(LocalDate.of(2024, 4, 7));
        scheduleB.setStartTime(LocalTime.of(10, 0));
        scheduleB.setEndTime(LocalTime.of(18, 0));
        scheduleB.setUser(userA);
        scheduleB.setWorkplace(workplaceA);
        scheduleB.setNote("오후 근무");

        scheduleC = new Schedule();
        scheduleC.setWorkDate(LocalDate.of(2024, 4, 6));
        scheduleC.setStartTime(LocalTime.of(9, 0));
        scheduleC.setEndTime(LocalTime.of(17, 0));
        scheduleC.setUser(userB);
        scheduleC.setWorkplace(workplaceB);
        scheduleC.setNote("다른 워크플레이스 근무");
    }

    @Test
    @DisplayName("Schedule 저장 및 ID 조회 성공")
    void testSaveAndFindById() {
        Schedule saved = scheduleRepository.save(scheduleA);

        Optional<Schedule> found = scheduleRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(scheduleA.getWorkDate(), found.get().getWorkDate());
        assertEquals(scheduleA.getStartTime(), found.get().getStartTime());
        assertEquals(scheduleA.getEndTime(), found.get().getEndTime());
        assertEquals(userA.getId(), found.get().getUser().getId());
        assertEquals(workplaceA.getId(), found.get().getWorkplace().getId());
        assertEquals("아침 근무", found.get().getNote());
    }

    @Test
    @DisplayName("User ID로 Schedule 목록 조회 성공")
    void testFindByUserIdSuccess() {
        scheduleRepository.save(scheduleA);
        scheduleRepository.save(scheduleB);

        List<Schedule> found = scheduleRepository.findByUser_Id(userA.getId());

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(s -> "아침 근무".equals(s.getNote())));
        assertTrue(found.stream().anyMatch(s -> "오후 근무".equals(s.getNote())));
    }

    @Test
    @DisplayName("Workplace ID로 Schedule 목록 조회 성공")
    void testFindByWorkplaceIdSuccess() {
        scheduleRepository.save(scheduleA);
        scheduleRepository.save(scheduleB);
        scheduleRepository.save(scheduleC);

        List<Schedule> found = scheduleRepository.findByWorkplace_Id(workplaceA.getId());

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(s -> workplaceA.getId().equals(s.getWorkplace().getId())));
    }

    @Test
    @DisplayName("Workplace ID와 User ID로 Schedule 목록 조회 성공")
    void testFindByWorkplaceIdAndUserIdSuccess() {
        scheduleRepository.save(scheduleA);
        scheduleRepository.save(scheduleB);
        scheduleRepository.save(scheduleC);

        List<Schedule> found = scheduleRepository.findByWorkplace_IdAndUser_Id(workplaceA.getId(), userA.getId());

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(s ->
            workplaceA.getId().equals(s.getWorkplace().getId()) &&
            userA.getId().equals(s.getUser().getId())));
    }

    @Test
    @DisplayName("Schedule 업데이트 성공")
    void testUpdateSchedule() {
        Schedule saved = scheduleRepository.save(scheduleA);
        saved.setNote("수정된 근무");
        saved.setEndTime(LocalTime.of(18, 0));
        scheduleRepository.save(saved);

        Optional<Schedule> found = scheduleRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("수정된 근무", found.get().getNote());
        assertEquals(LocalTime.of(18, 0), found.get().getEndTime());
    }

    @Test
    @DisplayName("Schedule 삭제 성공")
    void testDeleteSchedule() {
        Schedule saved = scheduleRepository.save(scheduleA);

        scheduleRepository.deleteById(saved.getId());

        assertTrue(scheduleRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("Schedule 수 조회 성공")
    void testCountSchedules() {
        scheduleRepository.save(scheduleA);
        scheduleRepository.save(scheduleB);
        scheduleRepository.save(scheduleC);

        assertEquals(3, scheduleRepository.count());
    }

    @Test
    @DisplayName("빈 Schedule 목록 조회")
    void testFindEmptySchedules() {
        List<Schedule> found = scheduleRepository.findByUser_Id(999L);

        assertTrue(found.isEmpty());
    }
}