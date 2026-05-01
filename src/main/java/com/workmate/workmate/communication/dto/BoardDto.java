package com.workmate.workmate.communication.dto;

import com.workmate.workmate.communication.entity.BoardType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class BoardDto {

    @Schema(description = "게시판 생성 요청 객체") // 추가
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request { // 게시판 생성시 사용 dto
        @Schema(description = "게시판 이름", example = "자유게시판")
        private String boardName;

        @Schema(description = "게시판 타입", example = "NORMAL or NOTICE")
        private BoardType type;

    }

    @Schema(description = "게시판 조회 관련 객체")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LookUp {
        @Schema(description = "게시판 고유 ID", example = "1")
        private Long boardId;

        @Schema(description = "게시판 타입", example = "NORMAL or NOTICE")
        private BoardType type;

        @Schema(description = "게시판 이름", example = "자유게시판")
        private String boardName;


    }

    @Schema(description = "게시판 Patch용 객체")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoardUpdate{

        @Schema(description = "게시판 타입", example = "NORMAL or NOTICE")
        private BoardType type;

        @Schema(description = "게시판 이름", example = "자유게시판")
        private String boardName;

    }

    @Schema(description = "게시판 DELETE용 객체")
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoardDelete{
        @Schema(description = "게시판 ID" ,example = "1")
        private Long boardId;

        @Schema(description = "게시판 타입", example = "NORMAL or NOTICE")
        private BoardType type;

        @Schema(description = "게시판 이름", example = "자유게시판")
        private String boardName;

    }

}

