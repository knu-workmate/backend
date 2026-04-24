package com.workmate.workmate.user.service;

import com.workmate.workmate.global.exception.UnauthorizedException;
import com.workmate.workmate.global.security.CurrentUser;
import com.workmate.workmate.user.dto.SearchResponse;
import com.workmate.workmate.user.dto.WorkPlaceRequest;
import com.workmate.workmate.user.entity.Workplace;
import com.workmate.workmate.user.repository.WorkplaceRepository;
import com.workmate.workmate.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkPlace Service 테스트")
class WorkPlaceServiceTest {

    @Mock
    private WorkplaceRepository workplaceRepository;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkPlaceService workPlaceService;

    @Test
    @DisplayName("직장 생성 성공 - ADMIN 권한")
    void createWorkplace_withAdminRole_success() {
        // Given
        WorkPlaceRequest request = new WorkPlaceRequest("삼성전자");
        Workplace workplace = new Workplace();
        workplace.setId(1L);
        workplace.setName("삼성전자");
        workplace.setInviteCode("SAMSUNG123");

        given(currentUser.getUserRole()).willReturn("ADMIN");
        given(workplaceRepository.save(any(Workplace.class))).willReturn(workplace);

        // When
        Workplace result = workPlaceService.createWorkplace(request);

        // Then
        assertNotNull(result);
        assertEquals("삼성전자", result.getName());
        assertNotNull(result.getInviteCode());
    }

    @Test
    @DisplayName("직장 생성 실패 - WORKER 권한")
    void createWorkplace_withWorkerRole_throwsException() {
        // Given
        WorkPlaceRequest request = new WorkPlaceRequest("삼성전자");
        given(currentUser.getUserRole()).willReturn("WORKER");

        // When & Then
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> workPlaceService.createWorkplace(request));

        assertEquals("관리자 권한이 필요합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("직장 검색 성공")
    void searchWorkplaces_validName_returnsSearchResponses() {
        // Given
        Workplace workplace1 = new Workplace();
        workplace1.setId(1L);
        workplace1.setName("삼성전자");
        workplace1.setInviteCode("SAMSUNG123");

        Workplace workplace2 = new Workplace();
        workplace2.setId(2L);
        workplace2.setName("삼성화학");
        workplace2.setInviteCode("SAMSUNG456");

        List<Workplace> workplaces = Arrays.asList(workplace1, workplace2);
        given(workplaceRepository.findByNameContaining("삼성")).willReturn(workplaces);

        // When
        List<SearchResponse> result = workPlaceService.getWorkplaces("삼성");

        // Then
        assertEquals(2, result.size());
        assertEquals("삼성전자", result.get(0).getName());
        assertEquals("삼성화학", result.get(1).getName());
    }

    @Test
    @DisplayName("직장 검색 - 검색 결과 없음")
    void searchWorkplaces_noResults_returnsEmptyList() {
        // Given
        given(workplaceRepository.findByNameContaining("없는회사")).willReturn(Arrays.asList());

        // When
        List<SearchResponse> result = workPlaceService.getWorkplaces("없는회사");

        // Then
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("초대코드 생성 유효성 확인")
    void createWorkplace_generatesUniqueInvitationCode() {
        // Given
        WorkPlaceRequest request1 = new WorkPlaceRequest("회사1");
        WorkPlaceRequest request2 = new WorkPlaceRequest("회사2");

        given(currentUser.getUserRole()).willReturn("ADMIN");

        Workplace workplace1 = new Workplace();
        workplace1.setId(1L);
        workplace1.setName("회사1");
        workplace1.setInviteCode("CODE123456");

        Workplace workplace2 = new Workplace();
        workplace2.setId(2L);
        workplace2.setName("회사2");
        workplace2.setInviteCode("CODE789ABC");

        given(workplaceRepository.save(any(Workplace.class)))
                .willReturn(workplace1)
                .willReturn(workplace2);

        // When
        Workplace result1 = workPlaceService.createWorkplace(request1);
        Workplace result2 = workPlaceService.createWorkplace(request2);

        // Then
        assertNotNull(result1.getInviteCode());
        assertNotNull(result2.getInviteCode());
    }

    @Test
    @DisplayName("직장 생성 시 이름이 정확히 저장됨")
    void createWorkplace_nameSavedCorrectly() {
        // Given
        String workplaceName = "LG전자";
        WorkPlaceRequest request = new WorkPlaceRequest(workplaceName);

        given(currentUser.getUserRole()).willReturn("ADMIN");

        Workplace expectedWorkplace = new Workplace();
        expectedWorkplace.setId(1L);
        expectedWorkplace.setName(workplaceName);
        expectedWorkplace.setInviteCode("LG12345");

        given(workplaceRepository.save(any(Workplace.class))).willReturn(expectedWorkplace);

        // When
        Workplace result = workPlaceService.createWorkplace(request);

        // Then
        assertEquals(workplaceName, result.getName());
    }
}
