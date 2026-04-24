package com.workmate.workmate.user.service;

import org.springframework.stereotype.Service;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.dto.WorkPlaceRequest;
import com.workmate.workmate.global.exception.UnauthorizedException;
import java.util.List;
import com.workmate.workmate.user.dto.SearchResponse;
import java.util.stream.Collectors;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.dto.UserInfo;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.dto.SetWorkPlace;


@Service
public class WorkPlaceService {
    private final WorkplaceRepository workplaceRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    public WorkPlaceService(WorkplaceRepository workplaceRepository, CurrentUser currentUser, UserRepository userRepository) {
        this.workplaceRepository = workplaceRepository;
        this.currentUser = currentUser;
        this.userRepository = userRepository;
    }

    public Workplace createWorkplace(WorkPlaceRequest request) {
        // 현재 로그인한 유저가 관리자 계정이 맞는가?
        if(!currentUser.getUserRole().equals("ADMIN")) {
            throw new UnauthorizedException("관리자 권한이 필요합니다.");
        }

        Workplace workplace = new Workplace();
        workplace.setName(request.getName());
        // 초대코드 생성 로직
        String invitationCode = generateInvitationCode();
        workplace.setInviteCode(invitationCode);
        return workplaceRepository.save(workplace);
    }

    public List<SearchResponse> getWorkplaces(String name) {
        List<Workplace> workplaces = workplaceRepository.findByNameContaining(name);
        return workplaces.stream()
                .map(workplace -> new SearchResponse(workplace.getId(), workplace.getName()))
                .collect(Collectors.toList());
    }

    public List<UserInfo> getWorkplaceUsers(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        Workplace workplace = user.getWorkplace();
        List<User> users = userRepository.findByWorkplace(workplace);
        return users.stream()
                .map(u -> new UserInfo(u.getId(), u.getName(), u.getRole()))
                .collect(Collectors.toList());
    }

    public UserInfo assignAdmin(Long currentUserId, Long newAdminId) {
        User currentUserEntity = userRepository.findById(currentUserId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        if (!currentUserEntity.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("관리자 권한이 필요합니다.");
        }

        User newAdminEntity = userRepository.findById(newAdminId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        newAdminEntity.setRole(Role.ADMIN);
        userRepository.save(newAdminEntity);
        return new UserInfo(newAdminEntity.getId(), newAdminEntity.getName(), newAdminEntity.getRole());
    }
    
    public SetWorkPlace setWorkplace(Long userId, Long workplaceId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        Workplace workplace = workplaceRepository.findById(workplaceId).orElseThrow(() -> new RuntimeException("사업장을 찾을 수 없습니다."));
        
        user.setWorkplace(workplace);
        userRepository.save(user);
        
        return new SetWorkPlace("사업장이 성공적으로 설정되었습니다.", workplace.getId(), workplace.getName());
    }

    private String generateInvitationCode() {
        // 간단한 랜덤 문자열 생성 로직 (실제 구현에서는 더 복잡한 로직을 사용할 수 있음)
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}
