package com.workmate.workmate.user.repository;

import com.workmate.workmate.WorkmateApplication;
import com.workmate.workmate.user.entity.Workplace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WorkmateApplication.class)
@Transactional
@DisplayName("Workplace Repository 테스트")
public class WorkplaceRepositoryTest {

    @Autowired
    private WorkplaceRepository workplaceRepository;

    private Workplace workplaceA;
    private Workplace workplaceB;

    @BeforeEach
    void setUp() {
        workplaceA = new Workplace();
        workplaceA.setName("워크플레이스 A");
        workplaceA.setInviteCode("INVITE-A");

        workplaceB = new Workplace();
        workplaceB.setName("워크플레이스 B");
        workplaceB.setInviteCode("INVITE-B");
    }

    @Test
    @DisplayName("Workplace 저장 및 ID 조회 성공")
    void testSaveAndFindById() {
        Workplace saved = workplaceRepository.save(workplaceA);

        Optional<Workplace> found = workplaceRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(workplaceA.getName(), found.get().getName());
        assertEquals(workplaceA.getInviteCode(), found.get().getInviteCode());
    }

    @Test
    @DisplayName("이름으로 Workplace 조회 성공")
    void testFindByNameSuccess() {
        workplaceRepository.save(workplaceA);

        List<Workplace> found = workplaceRepository.findByName("워크플레이스 A");

        assertEquals(1, found.size());
        assertEquals("INVITE-A", found.get(0).getInviteCode());
    }

    @Test
    @DisplayName("초대 코드로 Workplace 조회 성공")
    void testFindByInviteCodeSuccess() {
        workplaceRepository.save(workplaceA);

        Optional<Workplace> found = workplaceRepository.findByInviteCode("INVITE-A");

        assertTrue(found.isPresent());
        assertEquals(workplaceA.getName(), found.get().getName());
    }

    @Test
    @DisplayName("이름으로 Workplace 조회 실패 - 존재하지 않는 이름")
    void testFindByNameNotFound() {
        workplaceRepository.save(workplaceA);

        List<Workplace> found = workplaceRepository.findByName("존재하지않는워크플레이스");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("초대 코드로 Workplace 조회 실패 - 존재하지 않는 코드")
    void testFindByInviteCodeNotFound() {
        workplaceRepository.save(workplaceA);

        Optional<Workplace> found = workplaceRepository.findByInviteCode("NO-CODE");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("모든 Workplace 조회")
    void testFindAll() {
        workplaceRepository.save(workplaceA);
        workplaceRepository.save(workplaceB);

        List<Workplace> all = workplaceRepository.findAll();

        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(w -> "INVITE-A".equals(w.getInviteCode())));
        assertTrue(all.stream().anyMatch(w -> "INVITE-B".equals(w.getInviteCode())));
    }

    @Test
    @DisplayName("Workplace 삭제")
    void testDeleteWorkplace() {
        Workplace saved = workplaceRepository.save(workplaceA);

        workplaceRepository.deleteById(saved.getId());

        assertTrue(workplaceRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("Workplace 업데이트")
    void testUpdateWorkplace() {
        Workplace saved = workplaceRepository.save(workplaceA);
        saved.setName("수정된 워크플레이스");
        workplaceRepository.save(saved);

        Optional<Workplace> found = workplaceRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("수정된 워크플레이스", found.get().getName());
    }

    @Test
    @DisplayName("전체 Workplace 수 조회")
    void testCountWorkplaces() {
        workplaceRepository.save(workplaceA);
        workplaceRepository.save(workplaceB);

        assertEquals(2, workplaceRepository.count());
    }

    @Test
    @DisplayName("중복 초대 코드로 저장 실패")
    void testSaveDuplicateInviteCodeShouldFail() {
        workplaceRepository.save(workplaceA);

        Workplace duplicate = new Workplace();
        duplicate.setName("워크플레이스 A 복제");
        duplicate.setInviteCode("INVITE-A");

        assertThrows(Exception.class, () -> {
            workplaceRepository.save(duplicate);
            workplaceRepository.flush();
        });
    }
}
