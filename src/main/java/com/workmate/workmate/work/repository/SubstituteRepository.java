package com.workmate.workmate.work.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.workmate.workmate.work.entity.Substitute;
import com.workmate.workmate.work.entity.SubstituteStatus;
import java.util.List;
import java.util.Optional;

public interface SubstituteRepository extends JpaRepository<Substitute, Long> {
    List<Substitute> findByRequester_Id(Long requesterId);
    List<Substitute> findBySubstituteUser_Id(Long substituteUserId);
    List<Substitute> findByRequester_IdAndStatus(Long requesterId, SubstituteStatus status);

    // 우리 매장(Workplace)의 모든 대타 신청글 조회
    List<Substitute> findBySchedule_Workplace_Id(Long workplaceId);

    // ADMIN 전용, 매칭된 대타 리스트 반환
    List<Substitute> findByStatus(SubstituteStatus status);

}
