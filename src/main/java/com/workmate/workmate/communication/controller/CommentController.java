package com.workmate.workmate.communication.controller;

import com.workmate.workmate.communication.dto.CommentDto;
import com.workmate.workmate.communication.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment", description = "댓글 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<Long> createComment(
            @PathVariable Long postId,
            @RequestBody CommentDto.CommentRequest req) {
        return ResponseEntity.ok(commentService.createComment(postId, req));
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 모든 댓글을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<CommentDto.CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. (작성자 본인만 가능)")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
    //커밋 에러떄문에 다시 날리기 위한 주석
}