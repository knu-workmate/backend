package com.workmate.workmate.communication.repository;

import com.workmate.workmate.WorkmateApplication;
import com.workmate.workmate.communication.entity.Board;
import com.workmate.workmate.communication.entity.BoardType;
import com.workmate.workmate.communication.entity.Comment;
import com.workmate.workmate.communication.entity.Post;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.repository.WorkplaceRepository;
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
@DisplayName("Comment Repository 테스트")
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkplaceRepository workplaceRepository;

    @Autowired
    private BoardRepository boardRepository;

    private Workplace workplaceA;
    private Board boardA;
    private User userA;
    private User userB;
    private Post postA;
    private Post postB;
    private Comment commentA;
    private Comment commentB;

    @BeforeEach
    void setUp() {
        workplaceA = new Workplace();
        workplaceA.setName("워크플레이스 A");
        workplaceA.setInviteCode("INVITE-A");
        workplaceA = workplaceRepository.save(workplaceA);

        boardA = new Board();
        boardA.setWorkplace(workplaceA);
        boardA.setName("공지사항");
        boardA.setType(BoardType.NOTICE);
        boardA = boardRepository.save(boardA);

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

        postA = new Post();
        postA.setBoard(boardA);
        postA.setUser(userA);
        postA.setTitle("게시글 A");
        postA.setContent("내용 A");
        postA = postRepository.save(postA);

        postB = new Post();
        postB.setBoard(boardA);
        postB.setUser(userB);
        postB.setTitle("게시글 B");
        postB.setContent("내용 B");
        postB = postRepository.save(postB);

        commentA = new Comment();
        commentA.setPost(postA);
        commentA.setUser(userA);
        commentA.setContent("댓글 A");

        commentB = new Comment();
        commentB.setPost(postA);
        commentB.setUser(userB);
        commentB.setContent("댓글 B");
    }

    @Test
    @DisplayName("Comment 저장 및 ID 조회 성공")
    void testSaveAndFindById() {
        Comment saved = commentRepository.save(commentA);

        Optional<Comment> found = commentRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(commentA.getContent(), found.get().getContent());
        assertEquals(postA.getId(), found.get().getPost().getId());
        assertEquals(userA.getId(), found.get().getUser().getId());
    }

    @Test
    @DisplayName("Post ID로 Comment 목록 조회 성공")
    void testFindByPostIdSuccess() {
        commentRepository.save(commentA);
        commentRepository.save(commentB);

        List<Comment> found = commentRepository.findByPost_Id(postA.getId());

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(comment -> "댓글 A".equals(comment.getContent())));
        assertTrue(found.stream().anyMatch(comment -> "댓글 B".equals(comment.getContent())));
    }

    @Test
    @DisplayName("Comment 업데이트")
    void testUpdateComment() {
        Comment saved = commentRepository.save(commentA);
        saved.setContent("수정된 댓글");
        commentRepository.save(saved);

        Optional<Comment> found = commentRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("수정된 댓글", found.get().getContent());
    }

    @Test
    @DisplayName("Comment 삭제")
    void testDeleteComment() {
        Comment saved = commentRepository.save(commentA);

        commentRepository.deleteById(saved.getId());

        assertTrue(commentRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("Comment 수 조회")
    void testCountComments() {
        commentRepository.save(commentA);
        commentRepository.save(commentB);

        assertEquals(2, commentRepository.count());
    }
}
