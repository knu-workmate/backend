package com.workmate.workmate.communication.service;


import com.workmate.workmate.communication.dto.CommentDto;
import com.workmate.workmate.communication.entity.Comment;
import com.workmate.workmate.communication.entity.Post;
import com.workmate.workmate.communication.repository.CommentRepository;
import com.workmate.workmate.communication.repository.PostRepository;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    @Transactional
    public Long createComment(Long postId, CommentDto.CommentRequest req) {

        // 로그인한 유저 정보 조회 (기존 코드 유지)
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        //  게시글 존재 확인 (Optional을 사용하는 것이 정석입니다)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        //  댓글 엔티티 생성 및 저장
        Comment comment = new Comment();
        comment.setUser(user);      // 작성자 매핑
        comment.setPost(post);      // 게시글 매핑
        comment.setContent(req.getContent()); // DTO에서 내용 가져오기

        Comment savedComment = commentRepository.save(comment);

        // 저장된 댓글의 ID 반환
        return savedComment.getId();
    }

    @Transactional(readOnly = true)
    public List<CommentDto.CommentResponse> getComments(Long postId) {
        return commentRepository.findByPost_Id(postId).stream()
                .map(comment -> CommentDto.CommentResponse.builder()
                        .commentId(comment.getId())
                        .content(comment.getContent())
                        .authorName(comment.getUser().getName())
                        .authorId(comment.getUser().getId())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(currentUser.getUserId())) {
            throw new RuntimeException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }

}
