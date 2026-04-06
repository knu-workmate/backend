package com.workmate.workmate.communication.repository;

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
import com.workmate.workmate.communication.entity.Board;
import com.workmate.workmate.communication.entity.BoardType;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.WorkplaceRepository;

@SpringBootTest(classes = WorkmateApplication.class)
@Transactional
@DisplayName("Board Repository 테스트")
public class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private WorkplaceRepository workplaceRepository;

    private Workplace workplaceA;
    private Workplace workplaceB;
    private Board boardA;
    private Board boardB;

    @BeforeEach
    void setUp() {
        workplaceA = new Workplace();
        workplaceA.setName("워크플레이스 A");
        workplaceA.setInviteCode("INVITE-A");
        workplaceA = workplaceRepository.save(workplaceA);

        workplaceB = new Workplace();
        workplaceB.setName("워크플레이스 B");
        workplaceB.setInviteCode("INVITE-B");
        workplaceB = workplaceRepository.save(workplaceB);

        boardA = new Board();
        boardA.setWorkplace(workplaceA);
        boardA.setName("공지사항");
        boardA.setType(BoardType.NOTICE);

        boardB = new Board();
        boardB.setWorkplace(workplaceA);
        boardB.setName("자유게시판");
        boardB.setType(BoardType.NORMAL);
    }

    @Test
    @DisplayName("Board 저장 및 ID 조회 성공")
    void testSaveAndFindById() {
        Board saved = boardRepository.save(boardA);

        Optional<Board> found = boardRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(boardA.getName(), found.get().getName());
        assertEquals(boardA.getType(), found.get().getType());
        assertEquals(workplaceA.getId(), found.get().getWorkplace().getId());
    }

    @Test
    @DisplayName("Workplace ID로 Board 목록 조회 성공")
    void testFindByWorkplaceIdSuccess() {
        boardRepository.save(boardA);
        boardRepository.save(boardB);

        List<Board> found = boardRepository.findByWorkplace_Id(workplaceA.getId());

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(board -> "공지사항".equals(board.getName())));
        assertTrue(found.stream().anyMatch(board -> "자유게시판".equals(board.getName())));
    }

    @Test
    @DisplayName("Workplace ID와 이름으로 Board 조회 성공")
    void testFindByNameAndWorkplaceIdSuccess() {
        boardRepository.save(boardA);

        Optional<Board> found = boardRepository.findByNameAndWorkplace_Id("공지사항", workplaceA.getId());

        assertTrue(found.isPresent());
        assertEquals(BoardType.NOTICE, found.get().getType());
        assertEquals(workplaceA.getId(), found.get().getWorkplace().getId());
    }

    @Test
    @DisplayName("Workplace ID와 이름으로 Board 조회 실패")
    void testFindByNameAndWorkplaceIdNotFound() {
        boardRepository.save(boardA);

        Optional<Board> found = boardRepository.findByNameAndWorkplace_Id("없는 게시판", workplaceA.getId());

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Board 업데이트")
    void testUpdateBoard() {
        Board saved = boardRepository.save(boardA);
        saved.setName("수정된 게시판");
        saved.setType(BoardType.NORMAL);
        boardRepository.save(saved);

        Optional<Board> found = boardRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("수정된 게시판", found.get().getName());
        assertEquals(BoardType.NORMAL, found.get().getType());
    }

    @Test
    @DisplayName("Board 삭제")
    void testDeleteBoard() {
        Board saved = boardRepository.save(boardA);

        boardRepository.deleteById(saved.getId());

        assertTrue(boardRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("Board 수 조회")
    void testCountBoards() {
        boardRepository.save(boardA);
        boardRepository.save(boardB);

        assertEquals(2, boardRepository.count());
    }
}
