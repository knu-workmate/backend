package com.workmate.workmate.work.controller;

import com.workmate.workmate.work.dto.SubstituteDto;
import com.workmate.workmate.work.service.SubstituteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "대타 매칭 (Substitute)", description = "대타 신청 - 직원 지원 - 점주 승인 프로세스 API")
@RestController
@RequestMapping("/api/substitutes")
@RequiredArgsConstructor
public class SubstituteController {

    private final SubstituteService substituteService;

    //대타 신청

    @Operation(summary = "[직원] 대타 신청글 생성", description = "자신의 근무 스케줄을 대타 시장에 내놓습니다. (상태: REQUESTED)")
    @PostMapping
    public ResponseEntity<Long> createRequest(@RequestBody SubstituteDto.CreateRequest req) {
        return ResponseEntity.ok(substituteService.createSubstituteRequest(req));
    }

    //대타 지원

    @Operation(summary = "[직원] 대타 지원하기", description = "올라온 대타 요청에 지원합니다. 성공 시 점주 승인 대기 상태가 됩니다. (상태: PENDING_APPROVAL)")
    @PatchMapping("/{substituteId}/apply")
    public ResponseEntity<Void> apply(@PathVariable Long substituteId) {
        substituteService.applySubstitute(substituteId);
        return ResponseEntity.ok().build();
    }

    //대타 승인

    @Operation(summary = "[점주] 대타 최종 승인", description = "지원자가 발생한 대타 건을 최종 승인합니다. 스케줄은 유지되나 매칭이 완료됩니다. (상태: APPROVED)")
    @PatchMapping("/{substituteId}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long substituteId) {
        substituteService.approveSubstitute(substituteId);
        return ResponseEntity.ok().build();
    }

    // 점주 전용 승인 대기 목록 조회
    @Operation(summary = "[점주] 승인 대기 목록 조회", description = "현재 우리 매장에서 지원자가 발생하여 점주의 승인을 기다리는 대타 목록입니다.")
    @GetMapping("/pending-approvals")
    public ResponseEntity<List<SubstituteDto.Response>> getPendingList() {
        return ResponseEntity.ok(substituteService.getPendingApprovals());
    }

    /**
     * 조회 API: 직원용 구인 중 목록
     */
    @Operation(summary = "[직원] 지원 가능한 대타 조회", description = "현재 지원 가능한(REQUESTED 상태인) 대타 요청 목록을 가져옵니다.")
    @GetMapping("/available")
    public ResponseEntity<List<SubstituteDto.Response>> getAvailableList() {
        return ResponseEntity.ok(substituteService.getAvailableSubstitutes());
    }
}