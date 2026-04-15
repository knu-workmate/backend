package com.workmate.workmate.communication.service;

import com.workmate.workmate.communication.dto.BoardDto;
import com.workmate.workmate.communication.entity.Board;
import com.workmate.workmate.communication.repository.BoardRepository;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workmate.workmate.global.security.CurrentUser;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final WorkplaceRepository workplaceRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;


    //게시판 생성
    @Transactional
    public Long createBoard(BoardDto.Request req) {
        // 1. 현재 로그인한 유저 정보 조회
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // 2. 유저가 소속된 사업장 확인 (팀장님 요청 핵심!)
        Workplace workplace = user.getWorkplace();
        if (workplace == null) {
            throw new RuntimeException("소속된 사업장 정보가 없습니다.");
        }

        // 3. 게시판 엔티티 생성
        Board board = new Board();
        board.setName(req.getBoardName());
        board.setType(req.getType());
        board.setWorkplace(workplace); // 토큰에서 찾은 사업장 자동 할당

        return boardRepository.save(board).getId();
    }

    @Transactional(readOnly = true)
    public List<BoardDto.LookUp> getAllBoards() { // 파라미터에서 workplaceId 제거
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        Workplace workplace = user.getWorkplace();
        if (workplace == null) {
            throw new RuntimeException("소속된 사업장 정보가 없습니다.");
        }

        // 해당 사업장의 게시판만 필터링 조회
        return boardRepository.findByWorkplaceId(workplace.getId()).stream()
                .map(board -> new BoardDto.LookUp(
                        board.getId(),
                        board.getType(),
                        board.getName()
                )).collect(Collectors.toList());
    }


}
