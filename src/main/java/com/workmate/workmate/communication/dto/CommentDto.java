package com.workmate.workmate.communication.dto;

import com.workmate.workmate.communication.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


public class CommentDto {

    @Schema(description = "댓글 작성 요청 객체")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentRequest {
        @Schema(description = "댓글 본문", example = "확인했습니다. ")
        private String content;

    }

    @Schema(description = "댓글 응답 객체")
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentResponse {
        private Long commentId;
        private String content;
        private String authorName;
        private Long authorId;
        private LocalDateTime createdAt;
    }
}
