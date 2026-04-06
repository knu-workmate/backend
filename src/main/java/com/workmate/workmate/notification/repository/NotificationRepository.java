package com.workmate.workmate.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.workmate.workmate.notification.entity.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_Id(Long userId);
    List<Notification> findByUser_IdAndIsReadFalse(Long userId);
    List<Notification> findByUser_IdAndIsReadTrue(Long userId);
}
