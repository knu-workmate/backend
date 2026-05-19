package com.workmate.workmate.ai.entity;

public enum EmbeddingStatus {
    PENDING, // 임베딩이 아직 처리되지 않은 상태
    COMPLETED, // 임베딩이 성공적으로 완료된 상태
    FAILED // 임베딩 처리 중 오류가 발생한 상태
}