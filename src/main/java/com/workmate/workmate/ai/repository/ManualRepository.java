package com.workmate.workmate.ai.repository;

import com.workmate.workmate.ai.entity.Manual;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ManualRepository extends JpaRepository<Manual, Long> {
    List<Manual> findByCategoryId(Long categoryId);
}
