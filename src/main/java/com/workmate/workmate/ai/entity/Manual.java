package com.workmate.workmate.ai.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;

import java.time.LocalDateTime;

import com.workmate.workmate.ai.entity.EmbeddingStatus;
import com.workmate.workmate.ai.entity.ManualCategory;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "manual")
public class Manual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사업장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_id", nullable = false)
    private Workplace workplace;

    // 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ManualCategory category;

    // 메뉴얼 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 임베딩 벡터
    @Column(columnDefinition = "JSON")
    private String embedding;

    // 임베딩 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmbeddingStatus embeddingStatus = EmbeddingStatus.PENDING;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // 활성화 여부
    @Column(nullable = false)
    private Boolean isActive = true;

    // 생성일
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 수정일
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}