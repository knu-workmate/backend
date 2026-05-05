package com.workmate.workmate.work.service;

import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.work.dto.SubstituteDto;
import com.workmate.workmate.work.entity.Schedule;
import com.workmate.workmate.work.entity.Substitute;
import com.workmate.workmate.work.entity.SubstituteHistory;
import com.workmate.workmate.work.entity.SubstituteStatus;
import com.workmate.workmate.work.repository.ScheduleRepository;
import com.workmate.workmate.work.repository.SubstituteHistoryRepository;
import com.workmate.workmate.work.repository.SubstituteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubstituteService {

    private final SubstituteRepository substituteRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final SubstituteHistoryRepository historyRepository; // 주입 확인!

    /**
     * 1. 대타 신청 (이력 기록 추가)
     */
    @Transactional
    public Long createSubstituteRequest(SubstituteDto.CreateRequest req) {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 정보 없음"));

        Schedule schedule = scheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new RuntimeException("해당 스케줄 없음"));

        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인의 스케줄만 대타 신청을 할 수 있습니다.");
        }

        Substitute substitute = new Substitute();
        substitute.setSchedule(schedule);
        substitute.setRequester(user);
        substitute.setStatus(SubstituteStatus.REQUESTED);
        substitute.setNote(req.getNote()); // 사유 저장

        Substitute saved = substituteRepository.save(substitute);

        // [이력 추가]
        saveHistory(saved, "REQUESTED", "대타 요청 생성", user.getId());

        return saved.getId();
    }

    // 본인 매장의 대타 리스트 조회
    @Transactional(readOnly = true)
    public List<SubstituteDto.Response> getAvailableSubstitutes() {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 정보 없음"));

        return substituteRepository.findBySchedule_Workplace_Id(user.getWorkplace().getId()).stream()
                .filter(sub -> sub.getStatus() == SubstituteStatus.REQUESTED)
                .map(sub -> {
                    Schedule sche = sub.getSchedule();
                    return SubstituteDto.Response.builder()
                            .substituteId(sub.getId())
                            .scheduleId(sche.getId())
                            .requesterName(sub.getRequester().getName())
                            .shiftStartTime(LocalDateTime.of(sche.getWorkDate(), sche.getStartTime()))
                            .shiftEndTime(LocalDateTime.of(sche.getWorkDate(), sche.getEndTime()))
                            .status(sub.getStatus())
                            .createdAt(sub.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 2. 대타 지원 (이력 기록 추가)
     */
    @Transactional
    public void applySubstitute(Long substituteId) {
        Long currentUserId = currentUser.getUserId();
        Substitute substitute = substituteRepository.findById(substituteId)
                .orElseThrow(() -> new RuntimeException("대타 요청 없음"));

        if (substitute.getRequester().getId().equals(currentUserId)) {
            throw new RuntimeException("본인이 올린 대타 요청에는 지원할 수 없습니다.");
        }

        User volunteer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("유저 정보 없음"));

        substitute.setSubstituteUser(volunteer);
        substitute.setStatus(SubstituteStatus.PENDING_APPROVAL);

        // [이력 추가]
        saveHistory(substitute, "PENDING_APPROVAL", volunteer.getName() + "님이 지원함", currentUserId);
    }

    /**
     * 3. 점주 승인 (이력 기록 추가)
     */
    @Transactional
    public void approveSubstitute(Long substituteId) {
        String userRole = currentUser.getUserRole();
        if (!userRole.equals("ADMIN")) {
            throw new RuntimeException("권한이 없습니다.");
        }

        Substitute substitute = substituteRepository.findById(substituteId)
                .orElseThrow(() -> new RuntimeException("대타 요청을 찾을 수 없습니다."));

        if (substitute.getStatus() != SubstituteStatus.PENDING_APPROVAL) {
            throw new RuntimeException("승인 대기 중인 요청이 아닙니다.");
        }

        substitute.setStatus(SubstituteStatus.APPROVED);
        substitute.setProcessedAt(LocalDateTime.now());

        // [이력 추가]
        saveHistory(substitute, "APPROVED", "점주 최종 승인 완료", currentUser.getUserId());
    }

    // 점주 전용: 승인 대기 리스트
    @Transactional(readOnly = true)
    public List<SubstituteDto.Response> getPendingApprovals() {
        String userRole = currentUser.getUserRole();
        if (!userRole.equals("ADMIN")) {
            throw new RuntimeException("점주(ADMIN) 권한이 있는 사용자만 접근 가능합니다.");
        }

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 정보 없음"));

        if (user.getWorkplace() == null) {
            throw new RuntimeException("소속된 사업장이 없습니다.");
        }

        return substituteRepository.findBySchedule_Workplace_Id(user.getWorkplace().getId()).stream()
                .filter(sub -> sub.getStatus() == SubstituteStatus.PENDING_APPROVAL)
                .map(sub -> {
                    Schedule sche = sub.getSchedule();
                    return SubstituteDto.Response.builder()
                            .substituteId(sub.getId())
                            .scheduleId(sche.getId())
                            .requesterName(sub.getRequester().getName())
                            .substituteUserName(sub.getSubstituteUser() != null ? sub.getSubstituteUser().getName() : null)
                            .shiftStartTime(LocalDateTime.of(sche.getWorkDate(), sche.getStartTime()))
                            .shiftEndTime(LocalDateTime.of(sche.getWorkDate(), sche.getEndTime()))
                            .status(sub.getStatus())
                            .createdAt(sub.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 4. 점주 거절 (기존 이력 로직 유지 + 권한 체크)
     */
    @Transactional
    public void rejectSubstitute(Long substituteId, String reason) {
        String userRole = currentUser.getUserRole();
        if (!userRole.equals("ADMIN")) {
            throw new RuntimeException("권한이 없습니다.");
        }

        Substitute substitute = substituteRepository.findById(substituteId)
                .orElseThrow(() -> new RuntimeException("대타 요청 없음"));

        Long ownerId = currentUser.getUserId();

        // [이력 저장]
        saveHistory(substitute, "REJECTED_BY_OWNER", reason, ownerId);

        // [핵심] 상태 복구: 다시 다른 사람이 지원할 수 있게 REQUESTED로 변경
        substitute.setSubstituteUser(null);
        substitute.setStatus(SubstituteStatus.REQUESTED);
    }

    /**
     * [공통 유틸리티] 이력 저장 메서드
     */
    private void saveHistory(Substitute sub, String status, String reason, Long processorId) {
        SubstituteHistory history = new SubstituteHistory();
        history.setSubstitute(sub);
        history.setStatus(status);
        history.setReason(reason);
        history.setProcessorId(processorId);
        historyRepository.save(history);
    }
}