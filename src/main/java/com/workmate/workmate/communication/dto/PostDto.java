package com.workmate.workmate.communication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class PostDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PostCreateRequest",description = "게시글 작성 요청")
    public static class Request {
        @Schema(description = "게시글 제목", example = "테스트 게시글 제목")
        private String title;

        @Schema(description = "게시글 본문", example = "테스트 게시글 내용")
        private String content;
    }


    @Getter
    @AllArgsConstructor
    @Schema(description = "게시글 목록 응답 데이터")
    public static class PostListResponse {
        private Long postId;
        private String title;
        private String authorName; // User 테이블 에서 가져옴
        private Long authorId; //추가했습니다
        private Long boardId;      // Board 테이블 에서 가져옴
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "게시글 수정 요청")
    public static class UpdateRequest {
        @Schema(description = "수정할 제목")
        private String title;
        @Schema(description = "수정할 내용")
        private String content;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "게시글 상세 조회 응답")
    public static class PostDetailResponse {
        private Long postId;
        private String title;
        private String content; // 본문 포함
        private String authorName;
        private Long authorId;
        private LocalDateTime createdAt;
    }

}
