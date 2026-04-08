package com.workmate.workmate.communication.dto;

import com.workmate.workmate.communication.entity.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class BoardDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request { //작성
        private String boardName;
        private BoardType type;
        private Long workplaceId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LookUp {
        private Long boardId;
        private String boardName;
        private BoardType type;
    }

}

