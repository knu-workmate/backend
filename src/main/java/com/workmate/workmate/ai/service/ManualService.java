package com.workmate.workmate.ai.service;

import org.springframework.stereotype.Service;
import com.workmate.workmate.ai.entity.ManualCategory;
import com.workmate.workmate.ai.dto.ManualCategoryRequest;
import java.util.List;
import com.workmate.workmate.ai.repository.ManualCategoryRepository;
import com.workmate.workmate.ai.repository.ManualRepository;
import com.workmate.workmate.ai.entity.Manual;
import com.workmate.workmate.ai.dto.ManualDTO;
import java.util.ArrayList;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.entity.User;
import java.util.Optional;
import com.workmate.workmate.ai.dto.ManualRequest;
import java.time.LocalDateTime;
import com.workmate.workmate.ai.entity.EmbeddingStatus;

@Service
public class ManualService {
    private final ManualCategoryRepository manualCategoryRepository;
    private final ManualRepository manualRepository;
    private final UserRepository userRepository;

    public ManualService(ManualCategoryRepository manualCategoryRepository, ManualRepository manualRepository, UserRepository userRepository) {
        this.manualCategoryRepository = manualCategoryRepository;
        this.manualRepository = manualRepository;
        this.userRepository = userRepository;
    }

    /**
     * 사용자의 직장에 속한 매뉴얼 카테고리를 조회하는 메서드입니다.
     * @param userId 사용자의 ID
     * @return 사용자의 직장에 속한 매뉴얼 카테고리 리스트
     */
    public List<ManualCategory> getManualCategories(Long userId) {
        return manualCategoryRepository.findByWorkplaceId(userId);
    }


    /**
     * 특정 매뉴얼 카테고리에 속한 매뉴얼을 조회하는 메서드입니다.
     * @param categoryId 매뉴얼 카테고리의 ID
     * @return 해당 카테고리에 속한 매뉴얼 리스트
     */
    public List<Manual> getManualsByCategory(Long categoryId) {
        return manualRepository.findByCategoryId(categoryId);
    }

    /**
     * 사용자의 직장에 속한 매뉴얼 카테고리와 해당 카테고리에 속한 매뉴얼을 조회하는 메서드입니다.
     * @param userId 사용자의 ID
     * @return 사용자의 직장에 속한 매뉴얼 카테고리와 해당 카테고리에 속한 매뉴얼 리스트
     */
    public List<ManualDTO> getCategoriesAndManuals(Long userId) {
        List<ManualCategory> categories = manualCategoryRepository.findByWorkplaceId(userId);
        List<ManualDTO> result = new ArrayList<>();
        for (ManualCategory category : categories) {
            List<Manual> manuals = manualRepository.findByCategoryId(category.getId());
            ManualDTO dto = new ManualDTO();
            dto.setManualId(category.getId());
            dto.setTitle(category.getName());
            dto.setWorkplaceId(category.getWorkplace().getId());
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                dto.setRole(user.getRole());
            }
            result.add(dto);
        }
        return result;
    }


    /**
     * 매뉴얼 카테고리를 생성하는 메서드입니다. 동일한 이름의 카테고리가 존재하는 경우 예외를 발생시킵니다.
     * @param userId 사용자의 ID
     * @param request 매뉴얼 카테고리 생성 요청 객체
     * @return 생성된 매뉴얼 카테고리
     */
    public ManualCategory createManualCategory(Long userId, ManualCategoryRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            Optional<ManualCategory> existingCategory = manualCategoryRepository.findByNameAndWorkplaceId(request.getName(), user.getWorkplace().getId());
            if (existingCategory.isPresent()) {
                throw new IllegalArgumentException("이미 동일한 이름의 카테고리가 존재합니다.");
            }
            if (user.getRole() != Role.ADMIN) {
                throw new SecurityException("카테고리를 생성할 권한이 없습니다.");
            }
        }
        ManualCategory category = new ManualCategory();
        category.setName(request.getName());
        if (user != null) {
            category.setWorkplace(user.getWorkplace());
        }
        return manualCategoryRepository.save(category);
    }


    /**
     * 매뉴얼 카테고리를 수정하는 메서드입니다. 동일한 이름의 카테고리가 존재하는 경우 예외를 발생시킵니다.
     * @param userId 사용자의 ID
     * @param categoryId 수정할 매뉴얼 카테고리의 ID
     * @param request 매뉴얼 카테고리 수정 요청 객체
     * @return 수정된 매뉴얼 카테고리
     */
    public ManualCategory updateManualCategory(Long userId, Long categoryId, ManualCategoryRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() != Role.ADMIN) {
            throw new SecurityException("카테고리를 수정할 권한이 없습니다.");
        }
        ManualCategory category = manualCategoryRepository.findById(categoryId).orElse(null);
        if (category != null) {
            Optional<ManualCategory> existingCategory = manualCategoryRepository.findByNameAndWorkplaceId(request.getName(), category.getWorkplace().getId());
            if (existingCategory.isPresent() && !existingCategory.get().getId().equals(categoryId)) {
                throw new IllegalArgumentException("이미 동일한 이름의 카테고리가 존재합니다.");
            }
            category.setName(request.getName());
            return manualCategoryRepository.save(category);
        }
        return null;
    }


    /**
     * 매뉴얼 카테고리를 삭제하는 메서드입니다.
     * @param userId 사용자의 ID
     * @param categoryId 삭제할 매뉴얼 카테고리의 ID
     */
    public void deleteManualCategory(Long userId, Long categoryId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() != Role.ADMIN) {
            throw new SecurityException("카테고리를 삭제할 권한이 없습니다.");
        }
        manualCategoryRepository.deleteById(categoryId);
    }


    /**
     * 매뉴얼을 생성하는 메서드입니다.
     * @param userId 사용자의 ID
     * @param request 매뉴얼 생성 요청 객체
     * @return 생성된 매뉴얼
     */
    public Manual createManual(Long userId, ManualRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() != Role.ADMIN) {
            throw new SecurityException("매뉴얼을 생성할 권한이 없습니다.");
        }
        Manual manual = new Manual();
        if (user != null) {
            manual.setWorkplace(user.getWorkplace());
        }
        manual.setContent(request.getContent());
        manual.setCategory(manualCategoryRepository.findById(request.getCategoryId()).orElse(null));
        manual.setEmbeddingStatus(EmbeddingStatus.PENDING);
        return manualRepository.save(manual);
    }


    /**
     * 매뉴얼을 수정하는 메서드입니다.
     * @param userId 사용자의 ID
     * @param manualId 수정할 매뉴얼의 ID
     * @param request 매뉴얼 수정 요청 객체
     * @return 수정된 매뉴얼
     */
    public Manual updateManual(Long userId, Long manualId, ManualRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() != Role.ADMIN) {
            throw new SecurityException("매뉴얼을 수정할 권한이 없습니다.");
        }
        Manual manual = manualRepository.findById(manualId).orElse(null);
        if (manual != null) {
            manual.setContent(request.getContent());
            manual.setCategory(manualCategoryRepository.findById(request.getCategoryId()).orElse(null));
            manual.setUpdatedAt(LocalDateTime.now());
            return manualRepository.save(manual);
        }
        return null;
    }


    /**
     * 매뉴얼을 삭제하는 메서드입니다.
     * @param userId 사용자의 ID
     * @param manualId 삭제할 매뉴얼의 ID
     */
    public void deleteManual(Long userId, Long manualId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() != Role.ADMIN) {
            throw new SecurityException("매뉴얼을 삭제할 권한이 없습니다.");
        }
        manualRepository.deleteById(manualId);
    }
}
