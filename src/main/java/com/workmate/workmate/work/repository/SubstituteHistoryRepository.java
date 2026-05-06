package com.workmate.workmate.work.repository;

import com.workmate.workmate.work.entity.SubstituteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubstituteHistoryRepository extends JpaRepository<SubstituteHistory, Long> {

    // 특정 대타 요청(Substitute)의 이력을 최신순으로 조회하는 메서드
    List<SubstituteHistory> findBySubstitute_IdOrderByCreatedAtDesc(Long substituteId);
}