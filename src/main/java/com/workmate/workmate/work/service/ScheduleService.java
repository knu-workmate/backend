package com.workmate.workmate.work.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.work.dto.ScheduleDate;
import com.workmate.workmate.work.dto.ScheduleGetResponse;
import com.workmate.workmate.work.dto.ScheduleRequest;
import com.workmate.workmate.work.dto.ScheduleResponse;
import com.workmate.workmate.work.entity.Schedule;
import com.workmate.workmate.work.entity.Substitute;
import com.workmate.workmate.work.entity.SubstituteStatus;
import com.workmate.workmate.work.repository.ScheduleRepository;
import com.workmate.workmate.work.repository.SubstituteRepository;
import com.workmate.workmate.work.dto.EntireScheduleResponse;
import com.workmate.workmate.work.dto.ScheduleWorker;
import com.workmate.workmate.work.dto.SchedulePatchRequest;
import com.workmate.workmate.user.entity.Role;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final SubstituteRepository substituteRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, UserRepository userRepository, SubstituteRepository substituteRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.substituteRepository = substituteRepository;
    }

    /**
     * 스케줄 저장 메서드
     * @param scheduleRequest 저장할 스케줄 정보가 담긴 DTO 리스트
     * @param userId 스케줄을 저장할 사용자 ID
     * @return 저장된 스케줄 정보가 담긴 DTO 리스트
     */
    public List<ScheduleResponse> saveSchedule(List<ScheduleRequest> scheduleRequest, Long userId) {
        // 리스트를 순회하며 각 스케줄 요청 저장, 새 리스트에 저장된 스케줄 응답 추가
        List<ScheduleResponse> savedSchedules = new ArrayList<>();
        for (ScheduleRequest request : scheduleRequest) {
            // 해당 시간에 이미 스케줄이 존재하는 지 확인
            // startTime과 endTime이 일부만 겹쳐도 겹치는 것으로 간주
            List<Schedule> existingSchedules = scheduleRepository.findByUserIdAndWorkDate(userId, request.getWorkDate());
            boolean hasConflict = existingSchedules.stream().anyMatch(schedule ->
                (request.getStartTime().isBefore(schedule.getEndTime()) && request.getEndTime().isAfter(schedule.getStartTime()))
            );

            if (hasConflict) {
                // 충돌이 있는 경우 예외 처리 또는 적절한 로직 수행
                throw new IllegalArgumentException("해당 시간에 이미 스케줄이 존재합니다.");
            }

            // 충돌이 없는 경우 스케줄 저장
            Schedule schedule = new Schedule();
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            Workplace workplace = user.getWorkplace();
            schedule.setUser(user);
            schedule.setWorkDate(request.getWorkDate());
            schedule.setStartTime(request.getStartTime());
            schedule.setEndTime(request.getEndTime());
            schedule.setWorkplace(workplace);
            if(request.getNote() != null) {
                schedule.setNote(request.getNote());
            }
            scheduleRepository.save(schedule);
            ScheduleResponse response = new ScheduleResponse();
            response.setId(schedule.getId());
            response.setUserId(user.getId());
            response.setUserName(user.getName());
            response.setWorkplaceId(workplace.getId());
            response.setWorkplaceName(workplace.getName());
            response.setWorkDate(schedule.getWorkDate());
            response.setStartTime(schedule.getStartTime());
            response.setEndTime(schedule.getEndTime());
            response.setNote(schedule.getNote());
            savedSchedules.add(response);
        }
        return savedSchedules;
    }

    /**
     * 관리자 권한으로 스케줄 저장 메서드
     * @param scheduleRequest 저장할 스케줄 정보가 담긴 DTO 리스트
     * @param userId 스케줄을 저장할 대상 사용자 ID
     * @param currentUserId 현재 로그인한 관리자 사용자 ID
     * @return 저장된 스케줄 정보가 담긴 DTO 리스트
     */
    public List<ScheduleResponse> saveScheduleAdmin(List<ScheduleRequest> scheduleRequest, Long userId, Long currentUserId) {
        // 현재 유저가 대상 유저의 근무지 관리자인지 검증
        User targetUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));
        User adminUser = userRepository.findById(currentUserId).orElseThrow(() -> new IllegalArgumentException("관리자 사용자를 찾을 수 없습니다."));
        if (!adminUser.getWorkplace().getId().equals(targetUser.getWorkplace().getId()) || !adminUser.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
        return saveSchedule(scheduleRequest, userId);
    }

    /**
     * 스케줄 삭제 메서드
     * @param scheduleIds 삭제할 스케줄 ID 리스트
     * @param userId 스케줄을 삭제할 사용자 ID
     * @return 삭제된 스케줄 정보가 담긴 DTO 리스트
     */
    public List<ScheduleResponse> deleteSchedule(List<Long> scheduleIds, Long userId) {
        List<ScheduleResponse> deletedSchedules = new ArrayList<>();
        for (Long scheduleId : scheduleIds) {
            Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));
            if (!schedule.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("사용자에게 해당 스케줄이 존재하지 않습니다.");
            }

            // 해당 유저만 삭제할 수 있도록 추가 검증
            if(!schedule.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("사용자에게 해당 스케줄이 존재하지 않습니다.");
            }

            Workplace workplace = schedule.getUser().getWorkplace();
            ScheduleResponse response = new ScheduleResponse();
            response.setId(schedule.getId());
            response.setUserId(schedule.getUser().getId());
            response.setUserName(schedule.getUser().getName());
            response.setWorkplaceId(workplace.getId());
            response.setWorkplaceName(workplace.getName());
            response.setWorkDate(schedule.getWorkDate());
            response.setStartTime(schedule.getStartTime());
            response.setEndTime(schedule.getEndTime());
            response.setNote(schedule.getNote());
            deletedSchedules.add(response);

            scheduleRepository.delete(schedule);
        }
        return deletedSchedules;
    }

    /**
     * 관리자 권한으로 스케줄 삭제 메서드
     * @param scheduleIds 삭제할 스케줄 ID 리스트
     * @param currentUserId 현재 로그인한 관리자 사용자 ID
     * @return 삭제된 스케줄 정보가 담긴 DTO 리스트
     */
    public List<ScheduleResponse> deleteScheduleAdmin(List<Long> scheduleIds, Long currentUserId) {
        // 현재 유저가 관리자인지 검증
        User adminUser = userRepository.findById(currentUserId).orElseThrow(() -> new IllegalArgumentException("관리자 사용자를 찾을 수 없습니다."));
        if (!adminUser.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }

        // 포함된 모든 스케줄 id에 대해 해당 사업장의 스케줄인지 검증
        for (Long scheduleId : scheduleIds) {
            Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));
            if (!schedule.getUser().getWorkplace().getId().equals(adminUser.getWorkplace().getId())) {
                throw new IllegalArgumentException("해당 사업장의 스케줄이 아닌 스케줄이 포함되어 있습니다.");
            }
        }

        // 모든 스케줄이 해당 사업장의 스케줄이라면 삭제 진행
        List<ScheduleResponse> deletedSchedules = new ArrayList<>();
        for (Long scheduleId : scheduleIds) {
            Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));
            Workplace workplace = schedule.getUser().getWorkplace();
            ScheduleResponse response = new ScheduleResponse();
            response.setId(schedule.getId());
            response.setUserId(schedule.getUser().getId());
            response.setUserName(schedule.getUser().getName());
            response.setWorkplaceId(workplace.getId());
            response.setWorkplaceName(workplace.getName());
            response.setWorkDate(schedule.getWorkDate());
            response.setStartTime(schedule.getStartTime());
            response.setEndTime(schedule.getEndTime());
            response.setNote(schedule.getNote());
            deletedSchedules.add(response);

            scheduleRepository.delete(schedule);
        }
        return deletedSchedules;
    }

    public ScheduleResponse updateSchedule(SchedulePatchRequest schedulePatchRequest, Long userId) {
        Schedule schedule = scheduleRepository.findById(schedulePatchRequest.getScheduleId()).orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));
        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("사용자에게 해당 스케줄이 존재하지 않습니다.");
        }

        // 날짜는 변경 불가, 시간과 노트만 변경 가능
        if (schedulePatchRequest.getNewStartTime() != null) {
            schedule.setStartTime(schedulePatchRequest.getNewStartTime());
        }
        if (schedulePatchRequest.getNewEndTime() != null) {
            schedule.setEndTime(schedulePatchRequest.getNewEndTime());
        }
        if (schedulePatchRequest.getNewNote() != null) {
            schedule.setNote(schedulePatchRequest.getNewNote());
        }

        scheduleRepository.save(schedule);

        ScheduleResponse response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setUserId(schedule.getUser().getId());
        response.setUserName(schedule.getUser().getName());
        response.setWorkplaceId(schedule.getUser().getWorkplace().getId());
        response.setWorkplaceName(schedule.getUser().getWorkplace().getName());
        response.setWorkDate(schedule.getWorkDate());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());
        response.setNote(schedule.getNote());

        return response;
    }

    /**
     * 관리자 권한으로 스케줄 수정 메서드
     * @param schedulePatchRequest 수정할 스케줄 정보가 담긴 DTO
     * @param currentUserId 현재 로그인한 관리자 사용자 ID
     * @return 수정된 스케줄 정보가 담긴 DTO
     */
    public ScheduleResponse updateScheduleAdmin(SchedulePatchRequest schedulePatchRequest, Long currentUserId) {
        // 현재 유저가 관리자이고 해당 스케줄의 관리자인지 검증
        User adminUser = userRepository.findById(currentUserId).orElseThrow(() -> new IllegalArgumentException("관리자 사용자를 찾을 수 없습니다."));
        if (!adminUser.getRole().equals(Role.ADMIN) || !adminUser.getWorkplace().getId().equals(scheduleRepository.findById(schedulePatchRequest.getScheduleId()).orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다.")).getUser().getWorkplace().getId())) {
            throw new IllegalArgumentException("관리자 권한이 없거나 해당 스케줄의 관리자가 아닙니다.");
        }
        return updateSchedule(schedulePatchRequest, scheduleRepository.findById(schedulePatchRequest.getScheduleId()).orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다.")).getUser().getId());
    }

    /**
     * 특정 기간 동안의 스케줄 조회 메서드
     * @param userId 조회할 사용자 ID
     * @param startDate 조회 시작 날짜
     * @param endDate 조회 종료 날짜
     * @return 조회된 스케줄 정보가 담긴 DTO
     */
    public ScheduleGetResponse getScheduleByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        // userId로 스케줄 조회 후 날짜 범위에 해당하는 스케줄 필터링
        List<Schedule> schedules = scheduleRepository.findByUser_Id(userId);
        ScheduleGetResponse response = new ScheduleGetResponse();
        if (schedules.isEmpty()) {
            return response; // 빈 응답 반환
        }
        Schedule firstSchedule = schedules.get(0);
        response.setUserId(firstSchedule.getUser().getId());
        response.setUserName(firstSchedule.getUser().getName());
        response.setWorkplaceId(firstSchedule.getUser().getWorkplace().getId());
        response.setWorkplaceName(firstSchedule.getUser().getWorkplace().getName());

        List<ScheduleDate> scheduleDates = new ArrayList<>();
        // 내가 원래 근무하는 스케줄
        for (Schedule schedule : schedules) {
            if (!schedule.getWorkDate().isBefore(startDate) && !schedule.getWorkDate().isAfter(endDate)) {
                // 만약 현재 스케줄이 대타로 인해 변경된 일정이라면(내가 근무하지 않게된 일정)
                Optional<Substitute> substituteOpt = substituteRepository.findBySchedule_IdAndStatus(schedule.getId(), SubstituteStatus.APPROVED);
                if (substituteOpt.isPresent()) {
                    // 대타로 인해 변경된 일정이므로 해당 일정은 건너뛰고 다음 스케줄로 넘어감
                    continue;
                }

                ScheduleDate scheduleDate = new ScheduleDate();
                scheduleDate.setWorkDate(schedule.getWorkDate());
                scheduleDate.setStartTime(schedule.getStartTime());
                scheduleDate.setEndTime(schedule.getEndTime());
                scheduleDate.setNote(schedule.getNote());
                scheduleDates.add(scheduleDate);
            }
        }

        // 내가 대타로 근무하게된 스케줄
        List<Substitute> substituteSchedules = substituteRepository.findBySubstituteUser_IdAndStatus(userId, SubstituteStatus.APPROVED);
        for (Substitute substitute : substituteSchedules) {
            Schedule schedule = substitute.getSchedule();
            if (schedule.getWorkDate().isBefore(startDate) || schedule.getWorkDate().isAfter(endDate)) {
                continue;
            }
            ScheduleDate scheduleDate = new ScheduleDate();
            scheduleDate.setWorkDate(schedule.getWorkDate());
            scheduleDate.setStartTime(schedule.getStartTime());
            scheduleDate.setEndTime(schedule.getEndTime());
            // note : 000 근무자의 00:00 ~ 00:00 근무 대타
            if (schedule.getNote() != null) {
                String note = schedule.getNote() + String.format("\n%s 근무자의 %s ~ %s 근무 대타", schedule.getUser().getName(), schedule.getStartTime(), schedule.getEndTime());
                scheduleDate.setNote(note);
            } else {
                String note = String.format("%s 근무자의 %s ~ %s 근무 대타", schedule.getUser().getName(), schedule.getStartTime(), schedule.getEndTime());
                scheduleDate.setNote(note);
            }
            scheduleDates.add(scheduleDate);
        }

        // 날짜 순서대로 정렬
        scheduleDates.sort((s1, s2) -> s1.getWorkDate().compareTo(s2.getWorkDate()));

        response.setScheduleDates(scheduleDates);
        return response;
    }

    /**
     * 특정 기간 동안의 모든 근무자 스케줄 조회 메서드 (대타로 인해 변경된 일정도 포함)
     * @param userId 조회할 사용자 ID
     * @param startDate 조회 시작 날짜
     * @param endDate 조회 종료 날짜
     * @return 조회된 스케줄 정보가 담긴 DTO 리스트
     */
    public List<EntireScheduleResponse> getEntireSchedule(Long userId, LocalDate startDate, LocalDate endDate) {
        Workplace workplace = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")).getWorkplace();
        
        List<EntireScheduleResponse> response = new ArrayList<>();
       // 해당 기간의 모든 날짜를 방문하며 스케줄과 대타 정보를 매칭하여 응답 생성
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 해당 날짜에 해당하는 스케줄 조회
            List<Schedule> schedules = scheduleRepository.findByWorkplace_IdAndWorkDate(workplace.getId(), currentDate);
            // 비어있다면 date 1증가 후 continue
            if (schedules.isEmpty()) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            // 스케줄이 존재한다면 EntireScheduleResponse 생성 후 날짜 설정
            EntireScheduleResponse dailySchedule = new EntireScheduleResponse();
            dailySchedule.setWorkDate(currentDate);
            List<ScheduleWorker> scheduleWorkers = new ArrayList<>();

            // 스케줄 순회
            for (Schedule schedule : schedules) {
                // 해당 스케줄의 수락된 대타가 존재한다면
                Optional<Substitute> substituteOpt = substituteRepository.findBySchedule_IdAndStatus(schedule.getId(), SubstituteStatus.APPROVED);
                if (substituteOpt.isPresent()) {
                    Substitute substitute = substituteOpt.get();
                    // 대타로 인해 변경된 일정이므로 근무자는 대타 근무자로 설정
                    ScheduleWorker worker = new ScheduleWorker();
                    worker.setUserId(substitute.getSubstituteUser().getId());
                    worker.setUserName(substitute.getSubstituteUser().getName());
                    worker.setScheduleId(schedule.getId());
                    worker.setStartTime(schedule.getStartTime());
                    worker.setEndTime(schedule.getEndTime());
                    worker.setNote(String.format("%s 근무자의 %s ~ %s 근무 대타", schedule.getUser().getName(), schedule.getStartTime(), schedule.getEndTime()));
                    scheduleWorkers.add(worker);
                } else {
                    // 대타로 인해 변경된 일정이 아니므로 근무자는 원래 근무자로 설정
                    ScheduleWorker worker = new ScheduleWorker();
                    worker.setUserId(schedule.getUser().getId());
                    worker.setUserName(schedule.getUser().getName());
                    worker.setScheduleId(schedule.getId());
                    worker.setStartTime(schedule.getStartTime());
                    worker.setEndTime(schedule.getEndTime());
                    if(schedule.getNote() != null) {
                        worker.setNote(schedule.getNote());
                    }
                    scheduleWorkers.add(worker);
                }
            }

            dailySchedule.setScheduleWorkers(scheduleWorkers);
            response.add(dailySchedule);
            currentDate = currentDate.plusDays(1);
        }

        return response;
    }
}
