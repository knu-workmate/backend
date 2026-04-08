package com.workmate.workmate.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class PostDto {

    @Getter
    @AllArgsConstructor
    public static class PostResponseDto {
        private String postId; //게시글 id
        private String boardId; //
        private String userId;
        private String title;
        private String content;
    }

    @Getter
    @AllArgsConstructor
    public static class PostUpdateDto {
        private String postId; //게시글 id
        private String boardId; //
        private String userId;
        private String title;
        private String content;
    }
}
