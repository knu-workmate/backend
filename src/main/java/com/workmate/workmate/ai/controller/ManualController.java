package com.workmate.workmate.ai.controller;

// sprinframework
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;

// swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// entity
import com.workmate.workmate.ai.entity.ManualCategory;
import com.workmate.workmate.ai.entity.EmbeddingStatus;
import com.workmate.workmate.ai.entity.Manual;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.ai.dto.ManualCategoryRequest;
import com.workmate.workmate.ai.dto.ManualDTO;

// service
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;


@RestController
@RequestMapping("/manuals")
@Tag(name = "메뉴얼", description = "메뉴얼 관련 API")
public class ManualController {
    private final CurrentUser currentUser;

    public ManualController(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @GetMapping("/categories")
    @Operation(summary = "메뉴얼 대분류(카테고리) 조회", description = "모든 매뉴얼 대분류를 조회합니다.")
    public ResponseEntity<List<ManualCategory>> getManualCategories() {
        Long userId = currentUser.getUserId();
    }

    @GetMapping("/manuals")
    @Operation(summary = "메뉴얼 조회", description = "특정 카테고리에 속한 매뉴얼을 조회합니다.")
    public ResponseEntity<List<Manual>> getManualsByCategory(@RequestParam Long categoryId) {
        Long userId = currentUser.getUserId();
    }

    @GetMapping("/categoriesAndManuals")
    @Operation(summary = "메뉴얼 대분류와 매뉴얼 조회", description = "모든 매뉴얼 대분류와 해당 카테고리에 속한 매뉴얼을 조회합니다.")
    public ResponseEntity<List<ManualDTO>> getCategoriesAndManuals() {
        Long userId = currentUser.getUserId();
    }

    @PostMapping("/categories")
    @Operation(summary = "메뉴얼의 대분류(카테고리)를 생성", description = "새로운 매뉴얼 대분류를 생성합니다.")
    public ResponseEntity<ManualCategory> createManualCategory(@RequestBody ManualCategoryRequest request) {
        Long userId = currentUser.getUserId();
    }

    @PatchMapping("/categories/{categoryId}")
    @Operation(summary = "메뉴얼 대분류(카테고리)를 수정", description = "기존 매뉴얼 대분류의 이름을 수정합니다.")
    public ResponseEntity<ManualCategory> updateManualCategory(@PathVariable Long categoryId, @RequestBody ManualCategoryRequest request) {
        Long userId = currentUser.getUserId();
    }

    @DeleteMapping("/categories/{categoryId}")
    @Operation(summary = "메뉴얼 대분류(카테고리)를 삭제", description = "기존 매뉴얼 대분류를 삭제합니다.")
    public ResponseEntity<Void> deleteManualCategory(@PathVariable Long categoryId) {
        Long userId = currentUser.getUserId();
    }

    @PostMapping("/manuals")
    @Operation(summary = "메뉴얼을 생성", description = "새로운 매뉴얼을 생성합니다.")
    public ResponseEntity<Manual> createManual(@RequestBody Manual request) {
        Long userId = currentUser.getUserId();
    }

    @PatchMapping("/manuals/{manualId}")
    @Operation(summary = "메뉴얼을 수정", description = "기존 매뉴얼의 내용을 수정합니다.")
    public ResponseEntity<Manual> updateManual(@PathVariable Long manualId, @RequestBody Manual request) {
        Long userId = currentUser.getUserId();
    }

    @DeleteMapping("/manuals/{manualId}")
    @Operation(summary = "메뉴얼을 삭제", description = "기존 매뉴얼을 삭제합니다.")
    public ResponseEntity<Void> deleteManual(@PathVariable Long manualId) {
        Long userId = currentUser.getUserId();
    }

}
