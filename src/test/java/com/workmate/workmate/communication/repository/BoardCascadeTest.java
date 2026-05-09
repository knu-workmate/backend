package com.workmate.workmate.communication.repository;

import com.workmate.workmate.WorkmateApplication;
import com.workmate.workmate.communication.entity.*;
import com.workmate.workmate.user.entity.*;
import com.workmate.workmate.user.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WorkmateApplication.class)
@Transactional
@DisplayName("게시판 연쇄 삭제 테스트")
public class BoardCascadeTest {

    @Autowired private BoardRepository boardRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WorkplaceRepository workplaceRepository;
    @Autowired private EntityManager em; // 영속성 컨텍스트 강제 제어용

    @Test
    @DisplayName("게시판 삭제 시 모든 게시글과 댓글이 연쇄 삭제되어야 한다")
    void shouldDeleteEverythingCascade() {
        // [필독] 클래스 상단에 private Board boardA; 같은 게 남아있으면 안 됩니다!

        // 1. 배경 데이터 준비 (업장 및 유저)
        Workplace workplace = new Workplace();
        workplace.setName("테스트 매장");
        workplace.setInviteCode("TEST-12345");
        workplace = workplaceRepository.saveAndFlush(workplace);

        User user = new User();
        user.setName("작성자");
        user.setEmail("clean_test@test.com"); // 이메일 중복 방지용
        user.setPassword("1234");
        user.setRole(Role.ADMIN);
        user = userRepository.saveAndFlush(user);

        // 2. 게시판 생성 및 저장 (1층)
        Board board = new Board();
        board.setName("삭제될 게시판");
        board.setType(BoardType.NOTICE);
        board.setWorkplace(workplace);
        Board savedBoard = boardRepository.saveAndFlush(board);

        // 3. 게시글 생성 및 저장 (2층) - 리턴받은 savedBoard를 써야 함!
        Post post = new Post();
        post.setTitle("제목");
        post.setContent("내용");
        post.setBoard(savedBoard);
        post.setUser(user);
        Post savedPost = postRepository.saveAndFlush(post);

        // 4. 댓글 생성 및 저장 (3층) - 리턴받은 savedPost를 써야 함!
        Comment comment = new Comment();
        comment.setContent("댓글");
        comment.setPost(savedPost);
        comment.setUser(user);
        commentRepository.saveAndFlush(comment);

        // 5. 영속성 컨텍스트를 비워줌 (진짜 DB에서 지워지는지 확인하기 위한 에이스의 한 수)
        em.flush();
        em.clear();

        // --------------------------------------------------
        // [액션] 6. 게시판(최상위 부모) 삭제
        // --------------------------------------------------
        Board boardToDelete = boardRepository.findById(savedBoard.getId()).orElseThrow();
        boardRepository.delete(boardToDelete);
        boardRepository.flush();

        // --------------------------------------------------
        // [검증] 7. 전부 사라졌는지 확인
        // --------------------------------------------------
        assertTrue(boardRepository.findById(savedBoard.getId()).isEmpty(), "게시판이 삭제되지 않음");
        assertTrue(postRepository.findById(savedPost.getId()).isEmpty(), "게시글이 연쇄 삭제되지 않음 (Cascade 실패)");
    }
}