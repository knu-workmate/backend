package com.workmate.workmate.user.service;

import org.springframework.stereotype.Service;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.dto.ProfileResponse;
import com.workmate.workmate.user.dto.PasswordRequest;

@Service
public class UserService {
    UserRepository userRepository;
    WorkplaceRepository workplaceRepository;

    public UserService(UserRepository userRepository, WorkplaceRepository workplaceRepository) {
        this.userRepository = userRepository;
        this.workplaceRepository = workplaceRepository;
    }

    /**
     * 사용자 프로필 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return 사용자 프로필 정보
     */
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 사용자 정보를 기반으로 ProfileResponse 객체를 생성하여 반환
        return new ProfileResponse(user.getEmail(), user.getName(), user.getRole(), user.getWorkplace().getName());
    }


    /**
     * 사용자 프로필 정보를 수정합니다.
     * @param userId 사용자 ID
     * @param profileRequest 수정할 프로필 정보
     * @return 수정된 사용자 프로필 정보
     */
    public ProfileResponse updateProfile(Long userId, ProfileResponse profileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        ProfileResponse currentProfile = new ProfileResponse(user.getEmail(), user.getName(), user.getRole(), user.getWorkplace().getName());

        if (user.getEmail() != null) {
            user.setEmail(profileRequest.getEmail());
            currentProfile.setEmail(profileRequest.getEmail());
        }

        if (user.getName() != null) {
            user.setName(profileRequest.getName());
            currentProfile.setName(profileRequest.getName());
        }

        if (user.getRole() != null) {
            user.setRole(profileRequest.getRole());
            currentProfile.setRole(profileRequest.getRole());
        }

        if (!user.getWorkplace().getName().equals(profileRequest.getWorkplaceName())) {
            throw new RuntimeException("사업장 이름은 변경할 수 없습니다.");
        }

        userRepository.save(user);

        return currentProfile;
    }


    /**
     * 사용자 비밀번호를 수정합니다.
     * @param passwordRequest 비밀번호 수정 요청 객체
     */
    public void updatePassword(Long userId, PasswordRequest passwordRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!user.getPassword().equals(passwordRequest.getCurrentPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordRequest.getNewPassword().length() < 6) {
            throw new RuntimeException("새 비밀번호는 최소 6자 이상이어야 합니다.");
        }

        user.setPassword(passwordRequest.getNewPassword());
        userRepository.save(user);
    }
}
