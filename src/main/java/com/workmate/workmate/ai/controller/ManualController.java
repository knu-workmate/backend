package com.workmate.workmate.ai.controller;

// springframework
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

// swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

// entity
import com.workmate.workmate.ai.entity.ManualCategory;
import com.workmate.workmate.ai.entity.EmbeddingStatus;
import com.workmate.workmate.ai.entity.Manual;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.ai.dto.ManualCategoryRequest;
import com.workmate.workmate.ai.dto.ManualDTO;
import com.workmate.workmate.ai.service.ManualService;
import com.workmate.workmate.ai.dto.ManualRequest;


import java.util.List;


@RestController
@RequestMapping("/manuals")
@Tag(name = "매뉴얼", description = "매뉴얼 관련 API")
public class ManualController {
    private final CurrentUser currentUser;
    private final ManualService manualService;

    public ManualController(CurrentUser currentUser, ManualService manualService) {
        this.currentUser = currentUser;
        this.manualService = manualService;
    }

    @GetMapping("/categories")
    @Operation(summary = "매뉴얼 대분류(카테고리) 조회", description = "모든 매뉴얼 대분류를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "매뉴얼 대분류 조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ManualCategory.class))))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<List<ManualCategory>> getManualCategories() {
        Long userId = currentUser.getUserId();
        List<ManualCategory> categories = manualService.getManualCategories(userId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/manuals")
    @Operation(summary = "매뉴얼 조회", description = "특정 카테고리에 속한 매뉴얼을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "매뉴얼 조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Manual.class))))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<List<Manual>> getManualsByCategory(@RequestParam Long categoryId) {
        Long userId = currentUser.getUserId();
        List<Manual> manuals = manualService.getManualsByCategory(categoryId);
        return ResponseEntity.ok(manuals);
    }

    @GetMapping("/categoriesAndManuals")
    @Operation(summary = "매뉴얼 대분류와 매뉴얼 조회", description = "모든 매뉴얼 대분류와 해당 카테고리에 속한 매뉴얼을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "매뉴얼 대분류와 매뉴얼 조회 성공", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ManualDTO.class))))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<List<ManualDTO>> getCategoriesAndManuals() {
        Long userId = currentUser.getUserId();
        List<ManualDTO> categoriesAndManuals = manualService.getCategoriesAndManuals(userId);
        return ResponseEntity.ok(categoriesAndManuals);
    }

    @PostMapping("/categories")
    @Operation(summary = "매뉴얼의 대분류(카테고리)를 생성", description = "새로운 매뉴얼 대분류를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "매뉴얼 대분류 생성 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ManualCategory.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<ManualCategory> createManualCategory(@RequestBody ManualCategoryRequest request) {
        Long userId = currentUser.getUserId();
        ManualCategory category = manualService.createManualCategory(userId, request);
        return ResponseEntity.ok(category);
    }

    @PatchMapping("/categories/{categoryId}")
    @Operation(summary = "매뉴얼 대분류(카테고리)를 수정", description = "기존 매뉴얼 대분류의 이름을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "매뉴얼 대분류 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ManualCategory.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<ManualCategory> updateManualCategory(@PathVariable Long categoryId, @RequestBody ManualCategoryRequest request) {
        Long userId = currentUser.getUserId();
        ManualCategory category = manualService.updateManualCategory(userId, categoryId, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/categories/{categoryId}")
    @Operation(summary = "매뉴얼 대분류(카테고리)를 삭제", description = "기존 매뉴얼 대분류를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "매뉴얼 대분류 삭제 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<Void> deleteManualCategory(@PathVariable Long categoryId) {
        Long userId = currentUser.getUserId();
        manualService.deleteManualCategory(userId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/manuals")
    @Operation(summary = "매뉴얼을 생성", description = "새로운 매뉴얼을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "매뉴얼 생성 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Manual.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<Manual> createManual(@RequestBody ManualRequest request) {
        Long userId = currentUser.getUserId();
        Manual manual = manualService.createManual(userId, request);
        return ResponseEntity.ok(manual);
    }

    @PatchMapping("/manuals/{manualId}")
    @Operation(summary = "매뉴얼을 수정", description = "기존 매뉴얼의 내용을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "매뉴얼 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Manual.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<Manual> updateManual(@PathVariable Long manualId, @RequestBody ManualRequest request) {
        Long userId = currentUser.getUserId();
        Manual manual = manualService.updateManual(userId, manualId, request);
        return ResponseEntity.ok(manual);
    }

    @DeleteMapping("/manuals/{manualId}")
    @Operation(summary = "매뉴얼을 삭제", description = "기존 매뉴얼을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "매뉴얼 삭제 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = com.workmate.workmate.global.exception.ErrorResponse.class)))
    public ResponseEntity<Void> deleteManual(@PathVariable Long manualId) {
        Long userId = currentUser.getUserId();
        manualService.deleteManual(userId, manualId);
        return ResponseEntity.noContent().build();
    }

}
