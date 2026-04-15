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

    /* 게시판 생성 */
    @Operation(summary = "게시판 생성", description = "새로운 게시판을 생성함")
    @PostMapping
    public ResponseEntity<Long> createBoard(@RequestBody BoardDto.Request req) {
        Long boardId = boardService.createBoard(req);
        return ResponseEntity.ok(boardId);
    }

    /*사업장 게시판 전체 조회*/
    @Operation(summary = "사업장 게시판 조회", description = "사업장 번호로 게시판을 전부 조회합니다.")
    @GetMapping("/workplace/{workplaceId}")
    public ResponseEntity<List<BoardDto.LookUp>> getWorkplaceBoards(@PathVariable Long workplaceId) {
        List<BoardDto.LookUp> boards = boardService.getAllBoards(workplaceId);
        return ResponseEntity.ok(boards);
    }

    /*특정 게시판 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDto.LookUp> getBoard(@PathVariable Long boardId) {
        BoardDto.LookUp board = boardService.getBoard(boardId);
        return ResponseEntity.ok(board);
    }*/
}
