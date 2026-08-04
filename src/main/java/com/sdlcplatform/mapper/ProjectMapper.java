package com.sdlcplatform.mapper;

import com.sdlcplatform.dto.response.ProjectMemberResponse;
import com.sdlcplatform.dto.response.ProjectResponse;
import com.sdlcplatform.entity.Project;
import com.sdlcplatform.entity.ProjectMember;
import com.sdlcplatform.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    default ProjectResponse toResponse(Project project, long memberCount) {
        if (project == null) {
            return null;
        }
        User manager = project.getManager();
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .priority(project.getPriority())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .archived(project.isArchived())
                .managerId(manager != null ? manager.getId() : null)
                .managerName(manager != null ? manager.getFullName() : null)
                .memberCount(memberCount)
                .createdAt(project.getCreatedAt())
                .build();
    }

    default ProjectMemberResponse toMemberResponse(ProjectMember member) {
        if (member == null) {
            return null;
        }
        User user = member.getUser();
        return ProjectMemberResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roleInProject(member.getRoleInProject())
                .build();
    }

    default List<ProjectMemberResponse> toMemberResponseList(List<ProjectMember> members) {
        if (members == null) {
            return List.of();
        }
        return members.stream().map(this::toMemberResponse).toList();
    }
}