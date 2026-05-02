package com.workmate.workmate.communication.service;

import com.workmate.workmate.communication.dto.PostDto;
import com.workmate.workmate.communication.entity.Board;
import com.workmate.workmate.communication.entity.Post;
import com.workmate.workmate.communication.repository.BoardRepository;
import com.workmate.workmate.communication.repository.PostRepository;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드 생성자 자동 생성
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    @Transactional
    public Long createPost(Long boardId, PostDto.Request req) {
        // 1. 게시판 존재 확인 (DDL의 board 테이블 조회)
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("해당 게시판을 찾을 수 없습니다."));

        // 2. 작성자 정보 확인 (DDL의 user 테이블 조회)
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("작성자 정보를 찾을 수 없습니다."));

        // 3. 엔티티 생성 및 연관 관계 설정
        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setBoard(board); // FK (board_id)
        post.setUser(user);   // FK (user_id)

        // 4. 저장 후 ID 반환
        return postRepository.save(post).getId();
    }

    @Transactional(readOnly = true)
    public List<PostDto.PostListResponse> getPostList(Long boardId, int page, int size) {
        // size가 0이거나 너무 크면 기본값 10으로 고정하는 방어 로직
        int finalSize = (size > 0 && size <= 50) ? size : 10;

        // 페이징 설정 (페이지 번호, 한 페이지 당 개수, 정렬 방향)
        Pageable pageable = PageRequest.of(page, finalSize);

        return postRepository.findByBoard_IdOrderByCreatedAtDesc(boardId, pageable)
                .getContent() // Page 객체에서 실제 List만 추출
                .stream()
                .map(post -> new PostDto.PostListResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getUser().getName(),
                        post.getBoard().getId(),
                        post.getUser().getId(),
                        post.getCreatedAt()
                )).collect(Collectors.toList());
    }

    // 게시글 상세 조회

    @Transactional(readOnly = true)
    public PostDto.PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        return new PostDto.PostDetailResponse(
                post.getId(), post.getTitle(), post.getContent(),
                post.getUser().getName(), post.getUser().getId(), post.getCreatedAt()
        );
    }

     // 게시글 수정 (PATCH)
    @Transactional
    public void updatePost(Long postId, PostDto.UpdateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // [보안] 작성자 본인 확인
        if (!post.getUser().getId().equals(currentUser.getUserId())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
    }

    //게시글 삭제 (DELETE)
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // [보안] 작성자 본인 확인
        if (!post.getUser().getId().equals(currentUser.getUserId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}