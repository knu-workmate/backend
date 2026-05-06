package com.workmate.workmate.work.entity;

import java.time.LocalDateTime;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "substitute_history")
public class SubstituteHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 대타 요청에 대한 이력인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_id", nullable = false)
    private Substitute substitute;

    // 변경된 상태 (예: REQUESTED, PENDING_APPROVAL, APPROVED, REJECTED_BY_OWNER 등)
    @Column(nullable = false)
    private String status;

    // 거절 사유나 기타 비고 사항
    @Column(length = 255)
    private String reason;

    // 이 처리를 한 사람의 ID (사장님 혹은 지원자 ID)
    private Long processorId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}