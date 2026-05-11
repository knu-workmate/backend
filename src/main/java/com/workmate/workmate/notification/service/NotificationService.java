package com.workmate.workmate.notification.service;

import com.workmate.workmate.notification.dto.NotificationDto;
import com.workmate.workmate.notification.entity.Notification;
import com.workmate.workmate.notification.repository.NotificationRepository;
import com.workmate.workmate.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    //알림 생성 및 전송 (다른 서비스에서 호출용)
    @Transactional
    public void send(User receiver, String title, String content) {
        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setTitle(title);
        notification.setContent(content);
        notificationRepository.save(notification);
    }


     //현재 사용자의 모든 알림 목록 조회 (최신순)

    @Transactional(readOnly = true)
    public List<NotificationDto.Response> getMyNotifications(Long userId) {
        // 알림 목록을 가져와서 DTO로 변환
        return notificationRepository.findByUser_Id(userId).stream()
                .map(NotificationDto.Response::fromEntity)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // 최신순 정렬
                .collect(Collectors.toList());
    }


    //읽지 않은 알림 개수 조회

    @Transactional(readOnly = true)
    public NotificationDto.UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.findByUser_IdAndIsReadFalse(userId).size();
        return new NotificationDto.UnreadCountResponse(count);
    }


    //알림 읽음 처리
    @Transactional
    public void readNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("해당 알림을 찾을 수 없습니다."));

        // 본인의 알림인지 권한 체크
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("알림 읽기 권한이 없습니다.");
        }

        notification.markAsRead();
    }
}