package com.sdlcplatform.service;

import com.sdlcplatform.dto.request.AssignManagerRequest;
import com.sdlcplatform.dto.request.CreateProjectRequest;
import com.sdlcplatform.dto.response.ProjectResponse;
import com.sdlcplatform.entity.Project;
import com.sdlcplatform.entity.Role;
import com.sdlcplatform.entity.User;
import com.sdlcplatform.exception.InvalidOperationException;
import com.sdlcplatform.exception.ResourceNotFoundException;
import com.sdlcplatform.mapper.ProjectMapper;
import com.sdlcplatform.repository.ProjectMemberRepository;
import com.sdlcplatform.repository.ProjectRepository;
import com.sdlcplatform.repository.UserRepository;
import com.sdlcplatform.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectMapper projectMapper;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(projectRepository, projectMemberRepository, userRepository, projectMapper);
    }

    @Test
    void createProject_shouldThrow_whenManagerLacksEligibleRole() {
        UUID managerId = UUID.randomUUID();
        User ineligibleUser = User.builder()
                .email("dev@sdlcplatform.com")
                .roles(Set.of(Role.builder().name("DEVELOPER").build()))
                .build();

        CreateProjectRequest request = new CreateProjectRequest(
                "New Project", "desc", "HIGH", null, null, managerId);

        when(userRepository.findById(managerId)).thenReturn(Optional.of(ineligibleUser));

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void assignManager_shouldSucceed_whenUserHoldsProjectManagerRole() {
        UUID projectId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();

        Project project = Project.builder().name("Existing Project").build();
        User eligibleManager = User.builder()
                .email("pm@sdlcplatform.com")
                .roles(Set.of(Role.builder().name("PROJECT_MANAGER").build()))
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(managerId)).thenReturn(Optional.of(eligibleManager));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMemberRepository.countByProjectId(projectId)).thenReturn(0L);
        when(projectMapper.toResponse(project, 0L)).thenReturn(
                ProjectResponse.builder().id(projectId).managerName("pm@sdlcplatform.com").build());

        ProjectResponse response = projectService.assignManager(projectId, new AssignManagerRequest(managerId));

        org.assertj.core.api.Assertions.assertThat(response.getManagerName()).isEqualTo("pm@sdlcplatform.com");
    }

    @Test
    void deleteProject_shouldThrow_whenProjectNotArchived() {
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().name("Active Project").archived(false).build();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.deleteProject(projectId))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void getProjectById_shouldThrow_whenNotFound() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(projectId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}