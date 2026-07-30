package com.sdlcplatform.service;

import com.sdlcplatform.dto.request.AssignRolesRequest;
import com.sdlcplatform.dto.request.CreateUserRequest;
import com.sdlcplatform.dto.request.UpdateUserRequest;
import com.sdlcplatform.dto.request.UserSkillRequest;
import com.sdlcplatform.dto.response.PagedResponse;
import com.sdlcplatform.dto.response.UserResponse;
import com.sdlcplatform.dto.response.UserSkillResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    PagedResponse<UserResponse> listUsers(String search, String department, Boolean active, Pageable pageable);

    UserResponse getUserById(UUID id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    UserResponse deactivateUser(UUID id, String currentUserEmail);

    UserResponse reactivateUser(UUID id);

    UserResponse assignRoles(UUID id, AssignRolesRequest request);

    UserResponse getOwnProfile(String currentUserEmail);

    UserResponse updateOwnProfile(String currentUserEmail, UpdateUserRequest request);

    List<UserSkillResponse> getSkills(UUID userId);

    List<UserSkillResponse> replaceOwnSkills(String currentUserEmail, List<UserSkillRequest> skills);
}