package com.workmate.workmate.communication.service;

import com.workmate.workmate.communication.dto.BoardDto;
import com.workmate.workmate.communication.entity.Board;
import com.workmate.workmate.communication.repository.BoardRepository;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workmate.workmate.global.security.CurrentUser;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final WorkplaceRepository workplaceRepository;
    private final CurrentUser currentUser;


    public BoardService(BoardRepository boardRepository,
                        WorkplaceRepository workplaceRepository,
                        CurrentUser currentUser) {
        this.boardRepository = boardRepository;
        this.workplaceRepository = workplaceRepository;
        this.currentUser = currentUser;
    }


    //게시판 생성
    @Transactional
    public Long createBoard(BoardDto.Request req) {

        //로그인 한 유저 ID 체크
        Long uesrId = currentUser.getUserId();

        /*
        if (!currentUser.getUserRole().equals("ADMIN")){
            throw new RuntimeException("게시판 생성 권한이 없습니다.");
        }
         */

        Workplace workplace = workplaceRepository.findById(req.getWorkplaceId())
                .orElseThrow(() -> new RuntimeException("사업장을 찾을 수 없습니다."));

        Board board = new Board();
        board.setName(req.getBoardName());
        board.setType(req.getType());
        board.setWorkplace(workplace);

        return boardRepository.save(board).getId();
    }

    /**
     * 특정 사업장에 속한 모든 게시판 목록 조회
     */
    @Transactional(readOnly = true)
    public List<BoardDto.LookUp> getAllBoards(Long workplaceId) {
        // 해당 사업장이 있는지 먼저 확인
        workplaceRepository.findById(workplaceId)
                .orElseThrow(() -> new RuntimeException("사업장을 찾을 수 없습니다."));

        // Repository에서 리스트를 가져와 DTO 리스트로 변환
        return boardRepository.findByWorkplaceId(workplaceId).stream()
                .map(board -> new BoardDto.LookUp(board.getId(), board.getType(), board.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 게시판 단건 상세 조회
     */
    @Transactional(readOnly = true)
    public BoardDto.LookUp getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다."));

        return new BoardDto.LookUp(board.getId(), board.getType(), board.getName());
    }

}
