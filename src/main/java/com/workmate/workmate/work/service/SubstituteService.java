package com.workmate.workmate.work.service;

import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.work.dto.SubstituteDto;
import com.workmate.workmate.work.entity.Schedule;
import com.workmate.workmate.work.entity.Substitute;
import com.workmate.workmate.work.entity.SubstituteStatus;
import com.workmate.workmate.work.repository.ScheduleRepository;
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

   //대타신청
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

        return substituteRepository.save(substitute).getId();
    }

    //본인 매장의 대타 리스트 조회()
    @Transactional(readOnly = true)
    public List<SubstituteDto.Response> getAvailableSubstitutes() {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 정보 없음"));

        return substituteRepository.findBySchedule_Workplace_Id(user.getWorkplace().getId()).stream()
                // [수정] 지원 가능한 글은 오직 'REQUESTED' 상태여야 함
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

    //대타 지원
    @Transactional
    public void applySubstitute(Long substituteId) {
        User volunteer = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 정보 없음"));

        Substitute substitute = substituteRepository.findById(substituteId)
                .orElseThrow(() -> new RuntimeException("대타 요청 없음"));

        // REQUESTED(구인중) 상태일 때만 지원 가능
        if (substitute.getStatus() != SubstituteStatus.REQUESTED) {
            throw new RuntimeException("지원 가능한 상태가 아닙니다.");
        }

        substitute.setSubstituteUser(volunteer); // 지원자 등록
        substitute.setStatus(SubstituteStatus.PENDING_APPROVAL); // 상태 변경!
    }

   //점주전용: 대타 승인
    @Transactional
    public void approveSubstitute(Long substituteId) {
        Substitute substitute = substituteRepository.findById(substituteId)
                .orElseThrow(() -> new RuntimeException("대타 요청 없음"));

        if (substitute.getStatus() != SubstituteStatus.PENDING_APPROVAL) {
            throw new RuntimeException("승인 대기 중인 요청이 아닙니다.");
        }

        substitute.setStatus(SubstituteStatus.APPROVED); // 최종 승인!
        substitute.setProcessedAt(LocalDateTime.now());
    }

   //점주 전용: 대타 리스트
    @Transactional(readOnly = true)
    public List<SubstituteDto.Response> getPendingApprovals() {
        // 1. 현재 로그인한 사용자(점주) 정보 및 소속 매장 확인
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 정보 없음"));

        // 2. 해당 매장의 모든 대타 기록 중 'PENDING_APPROVAL' 상태만 필터링
        return substituteRepository.findBySchedule_Workplace_Id(user.getWorkplace().getId()).stream()
                .filter(sub -> sub.getStatus() == SubstituteStatus.PENDING_APPROVAL)
                .map(sub -> {
                    Schedule sche = sub.getSchedule();
                    return SubstituteDto.Response.builder()
                            .substituteId(sub.getId())
                            .scheduleId(sche.getId())
                            .requesterName(sub.getRequester().getName())
                            // 점주 화면에서 누가 지원했는지 보여야 하므로 추가
                            .substituteUserName(sub.getSubstituteUser() != null ? sub.getSubstituteUser().getName() : null)
                            .shiftStartTime(LocalDateTime.of(sche.getWorkDate(), sche.getStartTime()))
                            .shiftEndTime(LocalDateTime.of(sche.getWorkDate(), sche.getEndTime()))
                            .status(sub.getStatus())
                            .createdAt(sub.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}