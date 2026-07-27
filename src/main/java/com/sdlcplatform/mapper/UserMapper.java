package com.sdlcplatform.mapper;

import com.sdlcplatform.dto.response.UserResponse;
import com.sdlcplatform.entity.Role;
import com.sdlcplatform.entity.User;
import org.mapstruct.Mapper;

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
}
