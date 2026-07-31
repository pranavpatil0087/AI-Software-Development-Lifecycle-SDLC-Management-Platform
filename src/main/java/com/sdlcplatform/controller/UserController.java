package com.sdlcplatform.controller;

import com.sdlcplatform.dto.request.AssignRolesRequest;
import com.sdlcplatform.dto.request.CreateUserRequest;
import com.sdlcplatform.dto.request.UpdateUserRequest;
import com.sdlcplatform.dto.request.UserSkillRequest;
import com.sdlcplatform.dto.response.PagedResponse;
import com.sdlcplatform.dto.response.UserResponse;
import com.sdlcplatform.dto.response.UserSkillResponse;
import com.sdlcplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin user administration, self-service profile, roles, and skills")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "List users", description = "Paginated, searchable list of all users. Admin and Project Manager only.")
    public ResponseEntity<PagedResponse<UserResponse>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean active,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(search, department, active, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Get a user by id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a user", description = "Admin-only. Creates an active, pre-verified account with the given roles.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a user's profile fields", description = "Admin-only. Updates fullName/department/jobTitle for any user.")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a user", description = "Admin-only. Prevents login; an admin cannot deactivate their own account.")
    public ResponseEntity<UserResponse> deactivateUser(@Parameter(hidden = true) Authentication authentication,
                                                       @PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivateUser(id, authentication.getName()));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate a previously deactivated user")
    public ResponseEntity<UserResponse> reactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.reactivateUser(id));
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign roles", description = "Admin-only. Replaces the user's full role set with the roles provided.")
    public ResponseEntity<UserResponse> assignRoles(@PathVariable UUID id, @Valid @RequestBody AssignRolesRequest request) {
        return ResponseEntity.ok(userService.assignRoles(id, request));
    }

    @GetMapping("/{id}/skills")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Get a user's skills")
    public ResponseEntity<List<UserSkillResponse>> getSkills(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getSkills(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my own profile")
    public ResponseEntity<UserResponse> getOwnProfile(@Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(userService.getOwnProfile(authentication.getName()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my own profile", description = "Self-service update of fullName/department/jobTitle. Does not allow role changes.")
    public ResponseEntity<UserResponse> updateOwnProfile(@Parameter(hidden = true) Authentication authentication,
                                                         @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateOwnProfile(authentication.getName(), request));
    }

    @PutMapping("/me/skills")
    @Operation(summary = "Replace my own skills", description = "Replaces the caller's entire skill list with the one provided.")
    public ResponseEntity<List<UserSkillResponse>> replaceOwnSkills(@Parameter(hidden = true) Authentication authentication,
                                                                    @Valid @RequestBody List<UserSkillRequest> skills) {
        return ResponseEntity.ok(userService.replaceOwnSkills(authentication.getName(), skills));
    }
}