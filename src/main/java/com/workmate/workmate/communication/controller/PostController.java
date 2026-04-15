package com.workmate.workmate.communication.controller;

import com.workmate.workmate.communication.dto.PostDto;
import com.workmate.workmate.communication.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "게시글", description = "게시글 작성 및 조회를 담당합니다.")
@RestController
@RequestMapping("/boards/{boardId}/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 작성", description = "특정 게시판에 새로운 글을 작성합니다.")
    @PostMapping
    public ResponseEntity<Long> createPost(
            @Parameter(description = "게시판 ID") @PathVariable Long boardId,
            @RequestBody PostDto.Request req) {

        Long postId = postService.createPost(boardId, req);
        return ResponseEntity.ok(postId);
    }


    @Operation(summary = "게시글 목록 조회", description = "특정 게시판의 글 목록을 페이징하여 가져옵니다. 본문은 제외됩니다.")
    @GetMapping
    public ResponseEntity<List<PostDto.PostListResponse>> getPosts(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "0") int page,  // 몇 번째 페이지인지
            @RequestParam(defaultValue = "10") int size  // 한 번에 가져올 글 개수 (기본 10개)
    ) {
        return ResponseEntity.ok(postService.getPostList(boardId, page, size));
    }


}