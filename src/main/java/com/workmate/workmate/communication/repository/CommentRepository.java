package com.workmate.workmate.communication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.workmate.workmate.communication.entity.Comment;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_Id(Long postId);
}
