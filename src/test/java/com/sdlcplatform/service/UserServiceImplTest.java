package com.sdlcplatform.service;

import com.sdlcplatform.dto.request.CreateUserRequest;
import com.sdlcplatform.dto.response.UserResponse;
import com.sdlcplatform.entity.Role;
import com.sdlcplatform.entity.User;
import com.sdlcplatform.exception.EmailAlreadyExistsException;
import com.sdlcplatform.exception.InvalidOperationException;
import com.sdlcplatform.exception.ResourceNotFoundException;
import com.sdlcplatform.mapper.UserMapper;
import com.sdlcplatform.repository.RoleRepository;
import com.sdlcplatform.repository.UserRepository;
import com.sdlcplatform.repository.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserSkillRepository userSkillRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, roleRepository, userSkillRepository, passwordEncoder, userMapper);
    }

    @Test
    void createUser_shouldThrow_whenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(
                "Jane Dev", "jane@sdlcplatform.com", "Password123", "Engineering", "Engineer", Set.of("DEVELOPER"));
        when(userRepository.existsByEmail("jane@sdlcplatform.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void createUser_shouldThrow_whenRoleUnknown() {
        CreateUserRequest request = new CreateUserRequest(
                "Jane Dev", "jane@sdlcplatform.com", "Password123", "Engineering", "Engineer", Set.of("NOT_A_REAL_ROLE"));
        when(userRepository.existsByEmail("jane@sdlcplatform.com")).thenReturn(false);
        when(roleRepository.findByName("NOT_A_REAL_ROLE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_shouldCreateActiveAndVerifiedUser_onSuccess() {
        CreateUserRequest request = new CreateUserRequest(
                "Jane Dev", "Jane@SdlcPlatform.com", "Password123", "Engineering", "Engineer", Set.of("DEVELOPER"));

        Role developerRole = Role.builder().name("DEVELOPER").build();
        User savedUser = User.builder()
                .fullName("Jane Dev")
                .email("jane@sdlcplatform.com")
                .active(true)
                .emailVerified(true)
                .roles(Set.of(developerRole))
                .build();

        when(userRepository.existsByEmail("Jane@SdlcPlatform.com")).thenReturn(false);
        when(roleRepository.findByName("DEVELOPER")).thenReturn(Optional.of(developerRole));
        when(passwordEncoder.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(
                UserResponse.builder().id(UUID.randomUUID()).email("jane@sdlcplatform.com")
                        .active(true).emailVerified(true).build());

        UserResponse response = userService.createUser(request);

        assertThat(response.isActive()).isTrue();
        assertThat(response.isEmailVerified()).isTrue();
    }

    @Test
    void deactivateUser_shouldThrow_whenAdminTargetsOwnAccount() {
        UUID userId = UUID.randomUUID();
        User self = User.builder().email("admin@sdlcplatform.com").active(true).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> userService.deactivateUser(userId, "admin@sdlcplatform.com"))
                .isInstanceOf(InvalidOperationException.class);

        verify(userRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void deactivateUser_shouldSucceed_whenTargetingAnotherUser() {
        UUID userId = UUID.randomUUID();
        User target = User.builder().email("dev@sdlcplatform.com").active(true).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenReturn(target);
        when(userMapper.toResponse(target)).thenReturn(
                UserResponse.builder().email("dev@sdlcplatform.com").active(false).build());

        UserResponse response = userService.deactivateUser(userId, "admin@sdlcplatform.com");

        assertThat(response.isActive()).isFalse();
    }
}