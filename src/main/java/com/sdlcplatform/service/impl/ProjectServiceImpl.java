package com.sdlcplatform.service.impl;

import com.sdlcplatform.dto.request.AddProjectMemberRequest;
import com.sdlcplatform.dto.request.AssignManagerRequest;
import com.sdlcplatform.dto.request.CreateProjectRequest;
import com.sdlcplatform.dto.request.UpdateProjectRequest;
import com.sdlcplatform.dto.response.PagedResponse;
import com.sdlcplatform.dto.response.ProjectDashboardResponse;
import com.sdlcplatform.dto.response.ProjectMemberResponse;
import com.sdlcplatform.dto.response.ProjectResponse;
import com.sdlcplatform.entity.Project;
import com.sdlcplatform.entity.ProjectMember;
import com.sdlcplatform.entity.User;
import com.sdlcplatform.exception.InvalidOperationException;
import com.sdlcplatform.exception.ResourceNotFoundException;
import com.sdlcplatform.mapper.ProjectMapper;
import com.sdlcplatform.repository.ProjectMemberRepository;
import com.sdlcplatform.repository.ProjectRepository;
import com.sdlcplatform.repository.UserRepository;
import com.sdlcplatform.service.ProjectService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Override
    public PagedResponse<ProjectResponse> listProjects(String search, String status, String priority,
                                                       UUID managerId, Boolean archived, Pageable pageable) {
        Specification<Project> spec = buildSearchSpecification(search, status, priority, managerId, archived);
        Page<ProjectResponse> page = projectRepository.findAll(spec, pageable)
                .map(project -> projectMapper.toResponse(project, projectMemberRepository.countByProjectId(project.getId())));
        return PagedResponse.from(page);
    }

    @Override
    public ProjectResponse getProjectById(UUID id) {
        Project project = findProjectOrThrow(id);
        return projectMapper.toResponse(project, projectMemberRepository.countByProjectId(id));
    }

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Project.Priority priority = resolvePriority(request.getPriority());

        User manager = request.getManagerId() != null
                ? findManagerOrThrow(request.getManagerId())
                : null;

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(Project.Status.PLANNED.name())
                .priority(priority.name())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .archived(false)
                .manager(manager)
                .build();

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved, 0);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        Project project = findProjectOrThrow(id);

        if (StringUtils.hasText(request.getName())) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getStatus())) {
            project.setStatus(resolveStatus(request.getStatus()).name());
        }
        if (StringUtils.hasText(request.getPriority())) {
            project.setPriority(resolvePriority(request.getPriority()).name());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved, projectMemberRepository.countByProjectId(id));
    }

    @Override
    @Transactional
    public ProjectResponse archiveProject(UUID id) {
        Project project = findProjectOrThrow(id);
        project.setArchived(true);
        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved, projectMemberRepository.countByProjectId(id));
    }

    @Override
    @Transactional
    public ProjectResponse unarchiveProject(UUID id) {
        Project project = findProjectOrThrow(id);
        project.setArchived(false);
        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved, projectMemberRepository.countByProjectId(id));
    }

    @Override
    @Transactional
    public void deleteProject(UUID id) {
        Project project = findProjectOrThrow(id);
        if (!project.isArchived()) {
            throw new InvalidOperationException("Archive the project before deleting it");
        }
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectResponse assignManager(UUID id, AssignManagerRequest request) {
        Project project = findProjectOrThrow(id);
        User manager = findManagerOrThrow(request.getManagerId());
        project.setManager(manager);
        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved, projectMemberRepository.countByProjectId(id));
    }

    @Override
    public List<ProjectMemberResponse> listMembers(UUID id) {
        findProjectOrThrow(id);
        return projectMapper.toMemberResponseList(projectMemberRepository.findByProjectId(id));
    }

    @Override
    @Transactional
    public ProjectMemberResponse addMember(UUID id, AddProjectMemberRequest request) {
        Project project = findProjectOrThrow(id);

        if (projectMemberRepository.existsByProjectIdAndUserId(id, request.getUserId())) {
            throw new InvalidOperationException("This user is already a member of the project");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        String roleInProject = StringUtils.hasText(request.getRoleInProject())
                ? request.getRoleInProject().toUpperCase(Locale.ROOT)
                : ProjectMember.RoleInProject.CONTRIBUTOR.name();

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .roleInProject(roleInProject)
                .build();

        return projectMapper.toMemberResponse(projectMemberRepository.save(member));
    }

    @Override
    @Transactional
    public void removeMember(UUID id, UUID userId) {
        findProjectOrThrow(id);
        projectMemberRepository.findByProjectIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("This user is not a member of the project"));
        projectMemberRepository.deleteByProjectIdAndUserId(id, userId);
    }

    @Override
    public ProjectDashboardResponse getDashboard(UUID id) {
        Project project = findProjectOrThrow(id);
        long memberCount = projectMemberRepository.countByProjectId(id);

        return ProjectDashboardResponse.builder()
                .project(projectMapper.toResponse(project, memberCount))
                .memberCount(memberCount)
                .activeSprintCount(0)
                .openTaskCount(0)
                .openBugCount(0)
                .build();
    }

    private Project findProjectOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private User findManagerOrThrow(UUID managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + managerId));

        boolean isEligible = manager.getRoles().stream()
                .anyMatch(role -> role.getName().equals("PROJECT_MANAGER") || role.getName().equals("ADMIN"));

        if (!isEligible) {
            throw new InvalidOperationException("The assigned manager must hold the PROJECT_MANAGER or ADMIN role");
        }

        return manager;
    }

    private Project.Status resolveStatus(String status) {
        try {
            return Project.Status.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid status: " + status);
        }
    }

    private Project.Priority resolvePriority(String priority) {
        if (!StringUtils.hasText(priority)) {
            return Project.Priority.MEDIUM;
        }
        try {
            return Project.Priority.valueOf(priority.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid priority: " + priority);
        }
    }

    private Specification<Project> buildSearchSpecification(String search, String status, String priority,
                                                            UUID managerId, Boolean archived) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase(Locale.ROOT) + "%"));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status.toUpperCase(Locale.ROOT)));
            }
            if (StringUtils.hasText(priority)) {
                predicates.add(cb.equal(root.get("priority"), priority.toUpperCase(Locale.ROOT)));
            }
            if (managerId != null) {
                predicates.add(cb.equal(root.get("manager").get("id"), managerId));
            }
            if (archived != null) {
                predicates.add(cb.equal(root.get("archived"), archived));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}