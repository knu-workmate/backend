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
import com.workmate.workmate.communication.entity.Post;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.repository.WorkplaceRepository;

@SpringBootTest(classes = WorkmateApplication.class)
@Transactional
@DisplayName("Post Repository 테스트")
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkplaceRepository workplaceRepository;

    private Workplace workplaceA;
    private Board boardA;
    private Board boardB;
    private User userA;
    private User userB;
    private Post postA;
    private Post postB;

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

        boardB = new Board();
        boardB.setWorkplace(workplaceA);
        boardB.setName("자유게시판");
        boardB.setType(BoardType.NORMAL);
        boardB = boardRepository.save(boardB);

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
        postA.setTitle("첫 번째 게시글");
        postA.setContent("내용 A");

        postB = new Post();
        postB.setBoard(boardA);
        postB.setUser(userB);
        postB.setTitle("두 번째 게시글");
        postB.setContent("내용 B");
    }

    @Test
    @DisplayName("Post 저장 및 ID 조회 성공")
    void testSaveAndFindById() {
        Post saved = postRepository.save(postA);

        Optional<Post> found = postRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(postA.getTitle(), found.get().getTitle());
        assertEquals(postA.getContent(), found.get().getContent());
        assertEquals(boardA.getId(), found.get().getBoard().getId());
        assertEquals(userA.getId(), found.get().getUser().getId());
    }

    @Test
    @DisplayName("Board ID로 Post 목록 조회 성공")
    void testFindByBoardIdSuccess() {
        postRepository.save(postA);
        postRepository.save(postB);

        List<Post> found = postRepository.findByBoard_Id(boardA.getId());

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(post -> "첫 번째 게시글".equals(post.getTitle())));
        assertTrue(found.stream().anyMatch(post -> "두 번째 게시글".equals(post.getTitle())));
    }

    @Test
    @DisplayName("Board ID와 Post ID로 조회 성공")
    void testFindByIdAndBoardIdSuccess() {
        Post saved = postRepository.save(postA);

        Optional<Post> found = postRepository.findByIdAndBoard_Id(saved.getId(), boardA.getId());

        assertTrue(found.isPresent());
        assertEquals(postA.getTitle(), found.get().getTitle());
    }

    @Test
    @DisplayName("User ID로 Post 목록 조회 성공")
    void testFindByUserIdSuccess() {
        postRepository.save(postA);
        postRepository.save(postB);

        List<Post> found = postRepository.findByUser_Id(userA.getId());

        assertEquals(1, found.size());
        assertEquals("첫 번째 게시글", found.get(0).getTitle());
    }

    @Test
    @DisplayName("User ID와 Board ID로 Post 조회 성공")
    void testFindByUserIdAndBoardIdSuccess() {
        postRepository.save(postA);

        List<Post> found = postRepository.findByUser_IdAndBoard_Id(userA.getId(), boardA.getId());

        assertEquals(1, found.size());
        assertEquals("첫 번째 게시글", found.get(0).getTitle());
    }

    @Test
    @DisplayName("Post 업데이트")
    void testUpdatePost() {
        Post saved = postRepository.save(postA);
        saved.setTitle("수정된 제목");
        saved.setContent("수정된 내용");
        postRepository.save(saved);

        Optional<Post> found = postRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("수정된 제목", found.get().getTitle());
        assertEquals("수정된 내용", found.get().getContent());
    }

    @Test
    @DisplayName("Post 삭제")
    void testDeletePost() {
        Post saved = postRepository.save(postA);

        postRepository.deleteById(saved.getId());

        assertTrue(postRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("Post 수 조회")
    void testCountPosts() {
        postRepository.save(postA);
        postRepository.save(postB);

        assertEquals(2, postRepository.count());
    }
}
