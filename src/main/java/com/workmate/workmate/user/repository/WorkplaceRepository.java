package com.workmate.workmate.user.repository;

import com.workmate.workmate.user.entity.Workplace;
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.Optional;
import java.util.List;

public interface WorkplaceRepository extends JpaRepository<Workplace, Long> {
    List<Workplace> findByName(String name);
    Optional<Workplace> findByInviteCode(String inviteCode);
    List<Workplace> findByNameContaining(String name);
}
