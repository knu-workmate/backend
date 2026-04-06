package com.workmate.workmate.work.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.workmate.workmate.user.entity.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "substitute")
public class Substitute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 스케줄의 대타인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // 요청자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // 승인된 대타 사용자 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_user_id")
    private User substituteUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubstituteStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}