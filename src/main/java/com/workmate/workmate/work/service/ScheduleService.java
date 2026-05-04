package com.workmate.workmate.work.service;

import java.util.List;
import com.workmate.workmate.work.dto.ScheduleRequest;
import com.workmate.workmate.work.dto.ScheduleResponse;
import com.workmate.workmate.work.repository.ScheduleRepository;
import com.workmate.workmate.work.entity.Schedule;
import com.workmate.workmate.global.exception.ErrorResponse;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import java.util.ArrayList;
import com.workmate.workmate.work.dto.ScheduleGetResponse;
import com.workmate.workmate.work.dto.ScheduleDate;
import com.workmate.workmate.user.entity.User;
import org.springframework.stereotype.Service;
import com.workmate.workmate.work.repository.SubstituteRepository;
import com.workmate.workmate.work.entity.Substitute;
import java.util.Optional;
import com.workmate.workmate.work.entity.SubstituteStatus;

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

    public ScheduleGetResponse getScheduleByDateRange(Long userId, String startDate, String endDate) {
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
            if (schedule.getWorkDate().toString().compareTo(startDate) >= 0 && schedule.getWorkDate().toString().compareTo(endDate) <= 0) {
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
            if (schedule.getWorkDate().toString().compareTo(startDate) >= 0 && schedule.getWorkDate().toString().compareTo(endDate) <= 0) {
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
        }

        // 날짜 순서대로 정렬
        scheduleDates.sort((s1, s2) -> s1.getWorkDate().compareTo(s2.getWorkDate()));

        response.setScheduleDates(scheduleDates);
        return response;
    }
}
