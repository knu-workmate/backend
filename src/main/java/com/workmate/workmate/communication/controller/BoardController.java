package com.workmate.workmate.communication.controller;

import com.workmate.workmate.communication.dto.BoardDto;
import com.workmate.workmate.communication.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "게시판", description = "게시판 생성 및 조회 API")
@RestController
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @Operation(summary = "게시판 생성", description = "토큰의 유저 정보를 바탕으로 소속 사업장에 게시판을 생성함")
    @PostMapping
    public ResponseEntity<Long> createBoard(@RequestBody BoardDto.Request req) {
        return ResponseEntity.ok(boardService.createBoard(req));
    }

    @Operation(summary = "내 사업장 게시판 전체 조회", description = "로그인한 유저가 속한 사업장의 모든 게시판을 조회합니다.")
    @GetMapping("/my") // 경로에서 ID 제거하고 /my 등으로 변경
    public ResponseEntity<List<BoardDto.LookUp>> getMyWorkplaceBoards() {
        return ResponseEntity.ok(boardService.getAllBoards());
    }
}


