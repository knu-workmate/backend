package com.workmate.workmate.user.service;

import org.springframework.stereotype.Service;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.dto.WorkPlaceRequest;
import com.workmate.workmate.global.exception.UnauthorizedException;
import com.workmate.workmate.user.dto.WorkPlaceEdit;
import java.util.List;
import com.workmate.workmate.user.dto.SearchResponse;
import java.util.stream.Collectors;
import com.workmate.workmate.user.repository.UserRepository;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.dto.UserInfo;
import com.workmate.workmate.user.entity.Role;
import com.workmate.workmate.user.dto.SetWorkPlace;
import com.workmate.workmate.user.dto.WorkPlaceInfo;

@Service
public class WorkPlaceService {
    private final WorkplaceRepository workplaceRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    public WorkPlaceService(WorkplaceRepository workplaceRepository, CurrentUser currentUser,
            UserRepository userRepository) {
        this.workplaceRepository = workplaceRepository;
        this.currentUser = currentUser;
        this.userRepository = userRepository;
    }

    /**
     * 사업장 생성 메서드
     */
    public Workplace createWorkplace(WorkPlaceRequest request) {
        // 현재 로그인한 유저가 관리자 계정이 맞는가?
        if (!currentUser.getUserRole().equals("ADMIN")) {
            throw new UnauthorizedException("관리자 권한이 필요합니다.");
        }

        Workplace workplace = new Workplace();
        workplace.setName(request.getName());
        // 초대코드 생성 로직
        String invitationCode = generateInvitationCode();
        workplace.setInviteCode(invitationCode);
        return workplaceRepository.save(workplace);
    }

    /**
     * 사업장 정보 수정 메서드
     */
    public Workplace updateWorkplace(WorkPlaceEdit request) {
        // 현재 로그인한 유저가 관리자 계정이 맞는가?
        if (!currentUser.getUserRole().equals("ADMIN")) {
            throw new UnauthorizedException("관리자 권한이 필요합니다.");
        }

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        Workplace workplace = user.getWorkplace();
        workplace.setName(request.getName());
        return workplaceRepository.save(workplace);
    }

    /**
     * 사업장 정보 조회 메서드
     */
    public WorkPlaceInfo getWorkplaceInfo() {
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        Workplace workplace = user.getWorkplace();
        List<User> users = userRepository.findByWorkplace(workplace);
        List<UserInfo> userInfos = users.stream()
                .filter(u -> !u.getRole().equals(Role.ADMIN))
                .map(u -> new UserInfo(u.getId(), u.getName(), u.getRole()))
                .collect(Collectors.toList());

        List<UserInfo> adminInfos = users.stream()
                .filter(u -> u.getRole().equals(Role.ADMIN))
                .map(u -> new UserInfo(u.getId(), u.getName(), u.getRole()))
                .collect(Collectors.toList());

        WorkPlaceInfo workPlaceInfo = new WorkPlaceInfo();
        workPlaceInfo.setId(workplace.getId());
        workPlaceInfo.setName(workplace.getName());
        workPlaceInfo.setInviteCode(workplace.getInviteCode());
        workPlaceInfo.setCreatedAt(workplace.getCreatedAt().toString());
        workPlaceInfo.setUsers(userInfos);
        workPlaceInfo.setAdmins(adminInfos);

        return workPlaceInfo;
    }

    /**
     * 사업장 삭제 메서드
     */
    public WorkPlaceInfo deleteWorkplace() {
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("관리자 권한이 필요합니다.");
        }
        WorkPlaceInfo deletedWorkplace = getWorkplaceInfo();
        Workplace workplace = user.getWorkplace();
        List<User> users = userRepository.findByWorkplace(workplace);
        for (User u : users) {
            u.setWorkplace(null);
            userRepository.save(u);
        }
        workplaceRepository.delete(workplace);
        return deletedWorkplace;
    }

    /**
     * 사업장 검색 메서드
     */
    public List<SearchResponse> getWorkplaces(String name) {
        List<Workplace> workplaces = workplaceRepository.findByNameContaining(name);
        return workplaces.stream()
                .map(workplace -> new SearchResponse(workplace.getId(), workplace.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 사업장 사용자 조회 메서드
     */
    public List<UserInfo> getWorkplaceUsers(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        Workplace workplace = user.getWorkplace();
        List<User> users = userRepository.findByWorkplace(workplace);
        return users.stream()
                .map(u -> new UserInfo(u.getId(), u.getName(), u.getRole()))
                .collect(Collectors.toList());
    }

    /**
     * 관리자 권한 부여 메서드
     */
    public UserInfo assignAdmin(Long currentUserId, Long newAdminId) {
        User currentUserEntity = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        if (!currentUserEntity.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("관리자 권한이 필요합니다.");
        }

        User newAdminEntity = userRepository.findById(newAdminId)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        newAdminEntity.setRole(Role.ADMIN);
        userRepository.save(newAdminEntity);
        return new UserInfo(newAdminEntity.getId(), newAdminEntity.getName(), newAdminEntity.getRole());
    }

    /**
     * 관리자 권한 해제 메서드
     */
    public UserInfo removeAdmin(Long currentUserId, Long adminId) {
        User currentUserEntity = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        if (!currentUserEntity.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("관리자 권한이 필요합니다.");
        }

        User adminEntity = userRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        adminEntity.setRole(Role.WORKER);
        userRepository.save(adminEntity);
        return new UserInfo(adminEntity.getId(), adminEntity.getName(), adminEntity.getRole());
    }

    /**
     * 근무자 사업장 탈퇴 메서드
     */
    public UserInfo leaveWorkplace(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        if (user.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("관리자는 사업장을 탈퇴할 수 없습니다.");
        }
        user.setWorkplace(null);
        userRepository.save(user);
        return new UserInfo(user.getId(), user.getName(), user.getRole());
    }

    /**
     * 근무자 강제 퇴장 메서드
     */
    public UserInfo removeWorker(Long currentUserId, Long workerId) {
        User currentUserEntity = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        if (!currentUserEntity.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("관리자 권한이 필요합니다.");
        }

        User workerEntity = userRepository.findById(workerId)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        workerEntity.setWorkplace(null);
        userRepository.save(workerEntity);
        return new UserInfo(workerEntity.getId(), workerEntity.getName(), workerEntity.getRole());
    }

    /**
     * 사업장 설정 메서드 (사업장 ID로 설정)
     */
    public SetWorkPlace setWorkplaceById(Long userId, Long workplaceId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        Workplace workplace = workplaceRepository.findById(workplaceId)
                .orElseThrow(() -> new RuntimeException("사업장을 찾을 수 없습니다."));

        user.setWorkplace(workplace);
        userRepository.save(user);

        return new SetWorkPlace("사업장이 성공적으로 설정되었습니다.", workplace.getId(), workplace.getName());
    }

    /**
     * 사업장 설정 메서드 (초대코드로 설정)
     */
    public SetWorkPlace setWorkplaceByCode(Long userId, String inviteCode) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));
        Workplace workplace = workplaceRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 초대코드입니다."));

        user.setWorkplace(workplace);
        userRepository.save(user);

        return new SetWorkPlace("사업장이 성공적으로 설정되었습니다.", workplace.getId(), workplace.getName());
    }

    private String generateInvitationCode() {
        // 간단한 랜덤 문자열 생성 로직 (실제 구현에서는 더 복잡한 로직을 사용할 수 있음)
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}
