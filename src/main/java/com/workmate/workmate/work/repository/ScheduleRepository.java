package com.workmate.workmate.work.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.workmate.workmate.work.entity.Schedule;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUser_Id(Long userId);
    List<Schedule> findByWorkplace_IdAndUser_Id(Long workplaceId, Long userId);
    List<Schedule> findByWorkplace_Id(Long workplaceId);
}
