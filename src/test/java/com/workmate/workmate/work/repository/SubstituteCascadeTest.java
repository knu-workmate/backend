package com.workmate.workmate.work.repository;

import com.workmate.workmate.WorkmateApplication;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import com.workmate.workmate.work.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = WorkmateApplication.class)
@Transactional
@DisplayName("대타 관련 연쇄 삭제(Cascade) 기능 테스트")
public class SubstituteCascadeTest {

    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private SubstituteRepository substituteRepository;
    @Autowired private SubstituteHistoryRepository historyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WorkplaceRepository workplaceRepository;
    @Autowired private EntityManager em;

    @Test
    @DisplayName("스케줄을 삭제하면 연결된 대타 신청과 그 이력까지 모두 삭제되어야 한다")
    void shouldDeleteEverythingWhenScheduleIsDeleted() {
        // 1. 기초 데이터 준비 (업장 및 유저)
        Workplace workplace = new Workplace();
        workplace.setName("대타 테스트 매장");
        workplace.setInviteCode("SUB-TEST-999");
        workplace = workplaceRepository.saveAndFlush(workplace);

        User user = new User();
        user.setName("대타요청자");
        user.setEmail("sub_tester@test.com");
        user.setPassword("1234");
        user.setRole(Role.ADMIN);
        user = userRepository.saveAndFlush(user);

        // 2. 스케줄 생성 및 저장 (최상위 부모)
        Schedule schedule = new Schedule();
        schedule.setWorkDate(LocalDate.now());
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setUser(user);
        schedule.setWorkplace(workplace);
        Schedule savedSchedule = scheduleRepository.saveAndFlush(schedule);

        // 3. 대타 신청 생성 및 저장 (중간 부모)
        Substitute substitute = new Substitute();
        substitute.setSchedule(savedSchedule); // 저장된 스케줄 연결
        substitute.setRequester(user);
        substitute.setStatus(SubstituteStatus.REQUESTED);
        substitute.setNote("개인 사정으로 대타 구합니다.");
        Substitute savedSubstitute = substituteRepository.saveAndFlush(substitute);

        // 4. 대타 이력 생성 및 저장 (최하위 자식)
        SubstituteHistory history = new SubstituteHistory();
        history.setSubstitute(savedSubstitute); // 저장된 대타 신청 연결
        history.setStatus("REQUESTED");
        history.setReason("최초 신청 생성");
        history.setProcessorId(user.getId());
        historyRepository.saveAndFlush(history);

        // 5. 영속성 컨텍스트를 비워 실제 DB 상태로 만듦
        em.flush();
        em.clear();

        // --------------------------------------------------
        // [액션] 6. 최상위 부모인 '스케줄' 삭제
        // --------------------------------------------------
        Schedule scheduleToDelete = scheduleRepository.findById(savedSchedule.getId()).orElseThrow();
        scheduleRepository.delete(scheduleToDelete);
        scheduleRepository.flush(); // 연쇄 삭제 쿼리 실행!

        // --------------------------------------------------
        // [검증] 7. 할아버지부터 손자까지 다 지워졌는가?
        // --------------------------------------------------
        // 할아버지(Schedule) 확인
        assertTrue(scheduleRepository.findById(savedSchedule.getId()).isEmpty(), "스케줄이 지워지지 않음");

        // 아버지(Substitute) 확인
        assertTrue(substituteRepository.findById(savedSubstitute.getId()).isEmpty(), "대타 신청이 연쇄 삭제되지 않음");

        // 손자(SubstituteHistory) 확인
        assertTrue(historyRepository.findBySubstitute_IdOrderByCreatedAtDesc(savedSubstitute.getId()).isEmpty(), "대타 이력이 연쇄 삭제되지 않음");
    }
}