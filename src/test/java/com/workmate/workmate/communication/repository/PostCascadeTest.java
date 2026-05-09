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
@DisplayName("게시글 연쇄 삭제(Cascade) 기능 테스트")
public class PostCascadeTest {

    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WorkplaceRepository workplaceRepository;
    @Autowired private EntityManager em;

    @Test
    @DisplayName("게시글을 삭제하면 해당 게시글의 모든 댓글이 연쇄 삭제되어야 한다")
    void shouldDeleteCommentsWhenPostIsDeleted() {
        // 1. 배경 데이터 준비 (업장, 유저, 게시판)
        Workplace workplace = new Workplace();
        workplace.setName("게시글 테스트 매장");
        workplace.setInviteCode("POST-CASCADE-123");
        workplace = workplaceRepository.saveAndFlush(workplace);

        User user = new User();
        user.setName("작성자");
        user.setEmail("post_cascade@test.com");
        user.setPassword("1234");
        user.setRole(Role.ADMIN);
        user = userRepository.saveAndFlush(user);

        Board board = new Board();
        board.setName("테스트 게시판");
        board.setType(BoardType.NORMAL);
        board.setWorkplace(workplace);
        board = boardRepository.saveAndFlush(board);

        // 2. 게시글 저장
        Post post = new Post();
        post.setTitle("삭제될 게시글");
        post.setContent("내용");
        post.setBoard(board);
        post.setUser(user);
        Post savedPost = postRepository.saveAndFlush(post);

        // 3. 댓글 여러 개 저장 (연쇄 삭제 대상)
        for (int i = 1; i <= 3; i++) {
            Comment comment = new Comment();
            comment.setContent("댓글 " + i);
            comment.setPost(savedPost);
            comment.setUser(user);
            commentRepository.save(comment);
        }
        commentRepository.flush();

        // 4. 주머니(영속성 컨텍스트)를 완전히 비워서 쌩 리얼 DB 상태로 만듦
        em.clear();

        // --------------------------------------------------
        // [액션] 5. 게시글 삭제 (DELETE)
        // --------------------------------------------------
        // em.clear()를 했으므로 새로 찾아와서 삭제해야 함
        Post postToDelete = postRepository.findById(savedPost.getId()).orElseThrow();
        postRepository.delete(postToDelete);
        postRepository.flush(); // 연쇄 삭제 쿼리 실행!

        // --------------------------------------------------
        // [검증] 6. 전부 지워졌는지 확인
        // --------------------------------------------------
        // 게시글이 지워졌는가?
        assertTrue(postRepository.findById(savedPost.getId()).isEmpty(), "게시글이 삭제되지 않음");

        // 댓글들이 지워졌는가? (post_id로 조회 시 0개여야 함)
        List<Comment> remainingComments = commentRepository.findByPost_Id(savedPost.getId());
        assertTrue(remainingComments.isEmpty(), "댓글들이 연쇄 삭제되지 않음 (Cascade 실패)");

        // [에이스의 체크] 7. 게시판은 살아있어야 한다 (부모가 지워지면 안 됨!)
        assertTrue(boardRepository.findById(board.getId()).isPresent(), "게시글을 지웠는데 게시판까지 삭제됨 (잘못된 Cascade 설정)");
    }
}