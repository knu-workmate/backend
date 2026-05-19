package com.workmate.workmate.ai.repository;

import com.workmate.workmate.ai.entity.ManualCategory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ManualCategoryRepository extends JpaRepository<ManualCategory, Long> {
    List<ManualCategory> findByWorkplaceId(Long workplaceId);
    Optional<ManualCategory> findByNameAndWorkplaceId(String name, Long workplaceId);
}
