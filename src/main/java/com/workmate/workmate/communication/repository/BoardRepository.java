package com.workmate.workmate.communication.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workmate.workmate.communication.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByWorkplace_Id(Long workplaceId);

    Optional<Board> findByNameAndWorkplace_Id(String name, Long workplaceId);

    List<Board> findByWorkplaceId(Long workplaceId);
}
