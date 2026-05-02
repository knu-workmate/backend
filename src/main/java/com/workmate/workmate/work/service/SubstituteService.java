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
        // 1. currentUser에서 현재 로그인한 유저의 ID를 바로 가져옵니다.
        Long currentUserId = currentUser.getUserId();

        Substitute substitute = substituteRepository.findById(substituteId)
                .orElseThrow(() -> new RuntimeException("대타 요청 없음"));

        // 2. [해결] 신청자 ID와 현재 로그인한 유저 ID 비교
        if (substitute.getRequester().getId().equals(currentUserId)) {
            throw new RuntimeException("본인이 올린 대타 요청에는 지원할 수 없습니다.");
        }

        // ... 나머지 지원 로직
        User volunteer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("유저 정보 없음"));
        substitute.setSubstituteUser(volunteer);
        substitute.setStatus(SubstituteStatus.PENDING_APPROVAL);
    }

   //점주전용: 대타 승인
   @Transactional
   public void approveSubstitute(Long substituteId) {
       // 1. 현재 로그인한 유저의 Role 확인 (CurrentUser 활용)
       String userRole = currentUser.getUserRole();


       if (!userRole.equals("ADMIN")) {
           throw new RuntimeException("권한이 없습니다.");
       }

       Substitute substitute = substituteRepository.findById(substituteId)
               .orElseThrow(() -> new RuntimeException("대타 요청을 찾을 수 없습니다."));

       // 승인 대기 상태인지 확인
       if (substitute.getStatus() != SubstituteStatus.PENDING_APPROVAL) {
           throw new RuntimeException("승인 대기 중인 요청이 아닙니다.");
       }

       substitute.setStatus(SubstituteStatus.APPROVED);
       substitute.setProcessedAt(LocalDateTime.now());
   }

   //점주 전용: 대타 리스트
   @Transactional(readOnly = true)
   public List<SubstituteDto.Response> getPendingApprovals() {

       String userRole = currentUser.getUserRole();

       if (!userRole.equals("ADMIN")) {
           throw new RuntimeException("점주(ADMIN) 권한이 있는 사용자만 접근 가능합니다.");
       }

       // 유저 정보 및 소속 매장 확인
       User user = userRepository.findById(currentUser.getUserId())
               .orElseThrow(() -> new RuntimeException("유저 정보 없음"));

       // 매장에 등록되지 않은 경우 처리
       if (user.getWorkplace() == null) {
           throw new RuntimeException("소속된 사업장이 없습니다.");
       }

       // 3. 해당 매장의 모든 대타 기록 중 'PENDING_APPROVAL' 상태만 필터링
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
}