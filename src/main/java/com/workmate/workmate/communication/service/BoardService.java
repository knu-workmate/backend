package com.workmate.workmate.communication.service;

import com.workmate.workmate.communication.dto.BoardDto;
import com.workmate.workmate.communication.entity.Board;
import com.workmate.workmate.communication.repository.BoardRepository;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {

    private BoardRepository boardRepository;
    private WorkplaceRepository workplaceRepository;

    //게시판 생성

    public BoardService(BoardRepository boardRepository, WorkplaceRepository workplaceRepository) {
        this.boardRepository = boardRepository;
        this.workplaceRepository = workplaceRepository;
    }

    @Transactional
    public Long createBoard(BoardDto.Request req) {

        Workplace workplace = workplaceRepository.findById(req.getWorkplaceId())
                .orElseThrow(() -> new RuntimeException("사업장을 찾을 수 없습니다."));

        Board board = new Board();
        board.setName(req.getBoardName());
        board.setType(req.getType());
        board.setWorkplace(workplace);

        return 0L;
    }

}
