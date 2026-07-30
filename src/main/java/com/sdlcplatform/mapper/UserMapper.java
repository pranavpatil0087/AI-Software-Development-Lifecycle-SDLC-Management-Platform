package com.sdlcplatform.mapper;

import com.sdlcplatform.dto.response.UserResponse;
import com.sdlcplatform.dto.response.UserSkillResponse;
import com.sdlcplatform.entity.Role;
import com.sdlcplatform.entity.User;
import com.sdlcplatform.entity.UserSkill;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    default UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .department(user.getDepartment())
                .jobTitle(user.getJobTitle())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .roles(mapRoleNames(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    default Set<String> mapRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }

    default UserSkillResponse toSkillResponse(UserSkill skill) {
        if (skill == null) {
            return null;
        }
        return UserSkillResponse.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .proficiency(skill.getProficiency())
                .build();
    }

    default List<UserSkillResponse> toSkillResponseList(List<UserSkill> skills) {
        if (skills == null) {
            return List.of();
        }
        return skills.stream().map(this::toSkillResponse).toList();
    }
}