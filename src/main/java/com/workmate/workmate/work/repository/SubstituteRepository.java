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
}
