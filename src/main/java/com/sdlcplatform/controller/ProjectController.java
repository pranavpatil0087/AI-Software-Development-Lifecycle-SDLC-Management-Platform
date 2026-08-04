package com.sdlcplatform.controller;

import com.sdlcplatform.dto.request.AddProjectMemberRequest;
import com.sdlcplatform.dto.request.AssignManagerRequest;
import com.sdlcplatform.dto.request.CreateProjectRequest;
import com.sdlcplatform.dto.request.UpdateProjectRequest;
import com.sdlcplatform.dto.response.ApiResponse;
import com.sdlcplatform.dto.response.PagedResponse;
import com.sdlcplatform.dto.response.ProjectDashboardResponse;
import com.sdlcplatform.dto.response.ProjectMemberResponse;
import com.sdlcplatform.dto.response.ProjectResponse;
import com.sdlcplatform.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "Create, update, archive, and manage project membership")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "List projects", description = "Paginated, searchable list of projects.")
    public ResponseEntity<PagedResponse<ProjectResponse>> listProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID managerId,
            @RequestParam(required = false) Boolean archived,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(projectService.listProjects(search, status, priority, managerId, archived, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Get a project by id")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Create a project")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Update a project")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable UUID id, @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Archive a project")
    public ResponseEntity<ProjectResponse> archiveProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.archiveProject(id));
    }

    @PatchMapping("/{id}/unarchive")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Unarchive a project")
    public ResponseEntity<ProjectResponse> unarchiveProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.unarchiveProject(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a project", description = "Admin-only. The project must already be archived.")
    public ResponseEntity<ApiResponse> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.of("Project deleted successfully"));
    }

    @PatchMapping("/{id}/manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Assign a manager", description = "The target user must hold PROJECT_MANAGER or ADMIN.")
    public ResponseEntity<ProjectResponse> assignManager(@PathVariable UUID id, @Valid @RequestBody AssignManagerRequest request) {
        return ResponseEntity.ok(projectService.assignManager(id, request));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "List project members")
    public ResponseEntity<List<ProjectMemberResponse>> listMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.listMembers(id));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Add a project member")
    public ResponseEntity<ProjectMemberResponse> addMember(@PathVariable UUID id, @Valid @RequestBody AddProjectMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.addMember(id, request));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Remove a project member")
    public ResponseEntity<ApiResponse> removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        projectService.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.of("Member removed from project"));
    }

    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Get project dashboard", description = "Sprint/task/bug counts are placeholders until Phases 4-6 land.")
    public ResponseEntity<ProjectDashboardResponse> getDashboard(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getDashboard(id));
    }
}