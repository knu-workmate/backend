package com.workmate.workmate.ai.service;

import com.workmate.workmate.ai.dto.ManualCategoryRequest;
import com.workmate.workmate.ai.dto.ManualDTO;
import com.workmate.workmate.ai.dto.ManualRequest;
import com.workmate.workmate.ai.entity.EmbeddingStatus;
import com.workmate.workmate.ai.entity.Manual;
import com.workmate.workmate.ai.entity.ManualCategory;
import com.workmate.workmate.ai.repository.ManualCategoryRepository;
import com.workmate.workmate.ai.repository.ManualRepository;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("ManualService 테스트")
class ManualServiceTest {

    @Mock
    private ManualCategoryRepository manualCategoryRepository;

    @Mock
    private ManualRepository manualRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ManualService manualService;

    private User adminUser;
    private User normalUser;
    private Workplace workplace;
    private ManualCategory manualCategory;
    private Manual manual;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트용 데이터 초기화
        workplace = new Workplace();
        workplace.setId(1L);
        workplace.setName("Test Workplace");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("Admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setWorkplace(workplace);

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setName("Normal User");
        normalUser.setRole(Role.WORKER);
        normalUser.setWorkplace(workplace);

        manualCategory = new ManualCategory();
        manualCategory.setId(1L);
        manualCategory.setName("Test Category");
        manualCategory.setWorkplace(workplace);

        manual = new Manual();
        manual.setId(1L);
        manual.setCategory(manualCategory);
        manual.setContent("Test Manual Content");
        manual.setWorkplace(workplace);
        manual.setEmbeddingStatus(EmbeddingStatus.PENDING);
        manual.setCreatedBy(adminUser);
        manual.setIsActive(true);
    }

    @Test
    @DisplayName("사용자의 직장 매뉴얼 카테고리 조회 성공")
    void testGetManualCategoriesSuccess() {
        // Given
        Long userId = 1L;
        List<ManualCategory> categories = new ArrayList<>();
        categories.add(manualCategory);
        when(manualCategoryRepository.findByWorkplaceId(userId)).thenReturn(categories);

        // When
        List<ManualCategory> result = manualService.getManualCategories(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getName());
        verify(manualCategoryRepository, times(1)).findByWorkplaceId(userId);
    }

    @Test
    @DisplayName("특정 카테고리의 매뉴얼 조회 성공")
    void testGetManualsByCategorySuccess() {
        // Given
        Long categoryId = 1L;
        List<Manual> manuals = new ArrayList<>();
        manuals.add(manual);
        when(manualRepository.findByCategoryId(categoryId)).thenReturn(manuals);

        // When
        List<Manual> result = manualService.getManualsByCategory(categoryId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Manual Content", result.get(0).getContent());
        verify(manualRepository, times(1)).findByCategoryId(categoryId);
    }

    @Test
    @DisplayName("카테고리와 매뉴얼 함께 조회 성공")
    void testGetCategoriesAndManualsSuccess() {
        // Given
        Long userId = 1L;
        List<ManualCategory> categories = new ArrayList<>();
        categories.add(manualCategory);
        List<Manual> manuals = new ArrayList<>();
        manuals.add(manual);

        when(manualCategoryRepository.findByWorkplaceId(userId)).thenReturn(categories);
        when(manualRepository.findByCategoryId(1L)).thenReturn(manuals);
        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // When
        List<ManualDTO> result = manualService.getCategoriesAndManuals(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getTitle());
        assertEquals(Role.ADMIN, result.get(0).getRole());
        verify(manualCategoryRepository, times(1)).findByWorkplaceId(userId);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("관리자 권한으로 카테고리 생성 성공")
    void testCreateManualCategorySuccessAsAdmin() {
        // Given
        Long userId = 1L;
        ManualCategoryRequest request = new ManualCategoryRequest();
        request.setName("New Category");

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(manualCategoryRepository.findByNameAndWorkplaceId(request.getName(), workplace.getId()))
                .thenReturn(Optional.empty());
        when(manualCategoryRepository.save(any(ManualCategory.class))).thenReturn(manualCategory);

        // When
        ManualCategory result = manualService.createManualCategory(userId, request);

        // Then
        assertNotNull(result);
        assertEquals("Test Category", result.getName());
        verify(userRepository, times(1)).findById(userId);
        verify(manualCategoryRepository, times(1)).findByNameAndWorkplaceId(request.getName(), workplace.getId());
        verify(manualCategoryRepository, times(1)).save(any(ManualCategory.class));
    }

    @Test
    @DisplayName("일반 사용자가 카테고리 생성 시도 - 권한 없음 예외")
    void testCreateManualCategoryFailDueToLackOfPermission() {
        // Given
        Long userId = 2L;
        ManualCategoryRequest request = new ManualCategoryRequest();
        request.setName("New Category");

        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));

        // When & Then
        assertThrows(SecurityException.class, () -> {
            manualService.createManualCategory(userId, request);
        });
        verify(userRepository, times(1)).findById(userId);
        verify(manualCategoryRepository, never()).save(any(ManualCategory.class));
    }

    @Test
    @DisplayName("중복된 카테고리 이름으로 생성 시도 - 예외 발생")
    void testCreateManualCategoryFailDueToDuplicateName() {
        // Given
        Long userId = 1L;
        ManualCategoryRequest request = new ManualCategoryRequest();
        request.setName("Test Category");

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(manualCategoryRepository.findByNameAndWorkplaceId(request.getName(), workplace.getId()))
                .thenReturn(Optional.of(manualCategory));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            manualService.createManualCategory(userId, request);
        });
        verify(manualCategoryRepository, never()).save(any(ManualCategory.class));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void testUpdateManualCategorySuccess() {
        // Given
        Long userId = 1L;
        Long categoryId = 1L;
        ManualCategoryRequest request = new ManualCategoryRequest();
        request.setName("Updated Category");

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(manualCategoryRepository.findById(categoryId)).thenReturn(Optional.of(manualCategory));
        when(manualCategoryRepository.findByNameAndWorkplaceId(request.getName(), workplace.getId()))
                .thenReturn(Optional.empty());
        when(manualCategoryRepository.save(any(ManualCategory.class))).thenReturn(manualCategory);

        // When
        ManualCategory result = manualService.updateManualCategory(userId, categoryId, request);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(manualCategoryRepository, times(1)).findById(categoryId);
        verify(manualCategoryRepository, times(1)).save(any(ManualCategory.class));
    }

    @Test
    @DisplayName("카테고리 수정 - 권한 없음 예외")
    void testUpdateManualCategoryFailDueToLackOfPermission() {
        // Given
        Long userId = 2L;
        Long categoryId = 1L;
        ManualCategoryRequest request = new ManualCategoryRequest();
        request.setName("Updated Category");

        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));

        // When & Then
        assertThrows(SecurityException.class, () -> {
            manualService.updateManualCategory(userId, categoryId, request);
        });
        verify(manualCategoryRepository, never()).save(any(ManualCategory.class));
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void testDeleteManualCategorySuccess() {
        // Given
        Long userId = 1L;
        Long categoryId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // When
        manualService.deleteManualCategory(userId, categoryId);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(manualCategoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    @DisplayName("카테고리 삭제 - 권한 없음 예외")
    void testDeleteManualCategoryFailDueToLackOfPermission() {
        // Given
        Long userId = 2L;
        Long categoryId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));

        // When & Then
        assertThrows(SecurityException.class, () -> {
            manualService.deleteManualCategory(userId, categoryId);
        });
        verify(manualCategoryRepository, never()).deleteById(categoryId);
    }

    @Test
    @DisplayName("관리자 권한으로 매뉴얼 생성 성공")
    void testCreateManualSuccessAsAdmin() {
        // Given
        Long userId = 1L;
        ManualRequest request = new ManualRequest();
        request.setContent("New Manual Content");
        request.setCategoryId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(manualCategoryRepository.findById(1L)).thenReturn(Optional.of(manualCategory));
        when(manualRepository.save(any(Manual.class))).thenReturn(manual);

        // When
        Manual result = manualService.createManual(userId, request);

        // Then
        assertNotNull(result);
        assertEquals(EmbeddingStatus.PENDING, result.getEmbeddingStatus());
        verify(userRepository, times(1)).findById(userId);
        verify(manualCategoryRepository, times(1)).findById(1L);
        verify(manualRepository, times(1)).save(any(Manual.class));
    }

    @Test
    @DisplayName("일반 사용자가 매뉴얼 생성 시도 - 권한 없음 예외")
    void testCreateManualFailDueToLackOfPermission() {
        // Given
        Long userId = 2L;
        ManualRequest request = new ManualRequest();
        request.setContent("New Manual Content");
        request.setCategoryId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));

        // When & Then
        assertThrows(SecurityException.class, () -> {
            manualService.createManual(userId, request);
        });
        verify(manualRepository, never()).save(any(Manual.class));
    }

    @Test
    @DisplayName("매뉴얼 수정 성공")
    void testUpdateManualSuccess() {
        // Given
        Long userId = 1L;
        Long manualId = 1L;
        ManualRequest request = new ManualRequest();
        request.setContent("Updated Manual Content");
        request.setCategoryId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));
        when(manualRepository.findById(manualId)).thenReturn(Optional.of(manual));
        when(manualCategoryRepository.findById(1L)).thenReturn(Optional.of(manualCategory));
        when(manualRepository.save(any(Manual.class))).thenReturn(manual);

        // When
        Manual result = manualService.updateManual(userId, manualId, request);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(manualRepository, times(1)).findById(manualId);
        verify(manualRepository, times(1)).save(any(Manual.class));
    }

    @Test
    @DisplayName("매뉴얼 수정 - 권한 없음 예외")
    void testUpdateManualFailDueToLackOfPermission() {
        // Given
        Long userId = 2L;
        Long manualId = 1L;
        ManualRequest request = new ManualRequest();
        request.setContent("Updated Manual Content");
        request.setCategoryId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));

        // When & Then
        assertThrows(SecurityException.class, () -> {
            manualService.updateManual(userId, manualId, request);
        });
        verify(manualRepository, never()).save(any(Manual.class));
    }

    @Test
    @DisplayName("매뉴얼 삭제 성공")
    void testDeleteManualSuccess() {
        // Given
        Long userId = 1L;
        Long manualId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // When
        manualService.deleteManual(userId, manualId);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(manualRepository, times(1)).deleteById(manualId);
    }

    @Test
    @DisplayName("매뉴얼 삭제 - 권한 없음 예외")
    void testDeleteManualFailDueToLackOfPermission() {
        // Given
        Long userId = 2L;
        Long manualId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));

        // When & Then
        assertThrows(SecurityException.class, () -> {
            manualService.deleteManual(userId, manualId);
        });
        verify(manualRepository, never()).deleteById(manualId);
    }
}
