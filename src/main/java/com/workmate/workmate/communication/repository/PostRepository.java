package com.workmate.workmate.communication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.workmate.workmate.communication.entity.Post;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByBoard_Id(Long boardId);
    Optional<Post> findByIdAndBoard_Id(Long id, Long boardId);
    List<Post> findByUser_Id(Long userId);
    List<Post> findByUser_IdAndBoard_Id(Long userId, Long boardId);
}
