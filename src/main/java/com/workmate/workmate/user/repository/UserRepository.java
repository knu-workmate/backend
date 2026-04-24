package com.workmate.workmate.user.repository;

import com.workmate.workmate.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import com.workmate.workmate.user.entity.Workplace;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    List<User> findByWorkplace(Workplace workplace);
}
