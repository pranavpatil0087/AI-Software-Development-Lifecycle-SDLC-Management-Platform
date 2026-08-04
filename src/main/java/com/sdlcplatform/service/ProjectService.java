package com.sdlcplatform.service;

import com.sdlcplatform.dto.request.AddProjectMemberRequest;
import com.sdlcplatform.dto.request.AssignManagerRequest;
import com.sdlcplatform.dto.request.CreateProjectRequest;
import com.sdlcplatform.dto.request.UpdateProjectRequest;
import com.sdlcplatform.dto.response.PagedResponse;
import com.sdlcplatform.dto.response.ProjectDashboardResponse;
import com.sdlcplatform.dto.response.ProjectMemberResponse;
import com.sdlcplatform.dto.response.ProjectResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    PagedResponse<ProjectResponse> listProjects(String search, String status, String priority,
                                                UUID managerId, Boolean archived, Pageable pageable);

    ProjectResponse getProjectById(UUID id);

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse updateProject(UUID id, UpdateProjectRequest request);

    ProjectResponse archiveProject(UUID id);

    ProjectResponse unarchiveProject(UUID id);

    void deleteProject(UUID id);

    ProjectResponse assignManager(UUID id, AssignManagerRequest request);

    List<ProjectMemberResponse> listMembers(UUID id);

    ProjectMemberResponse addMember(UUID id, AddProjectMemberRequest request);

    void removeMember(UUID id, UUID userId);

    ProjectDashboardResponse getDashboard(UUID id);
}