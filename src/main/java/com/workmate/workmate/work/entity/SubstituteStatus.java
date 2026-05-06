package com.workmate.workmate.work.entity;

public enum SubstituteStatus {
    REQUESTED,        // 대타 구하는 중
    PENDING_APPROVAL, // 지원자 발생 (사장님 승인 대기)
    APPROVED,         // 사장님 승인 완료
    REJECTED          // 거절됨
}