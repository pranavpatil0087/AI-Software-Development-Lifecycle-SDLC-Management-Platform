package com.sdlcplatform.service;

import com.sdlcplatform.config.AppProperties;
import com.sdlcplatform.dto.request.RegisterRequest;
import com.sdlcplatform.dto.response.UserResponse;
import com.sdlcplatform.entity.Role;
import com.sdlcplatform.entity.User;
import com.sdlcplatform.exception.EmailAlreadyExistsException;
import com.sdlcplatform.exception.ResourceNotFoundException;
import com.sdlcplatform.mapper.UserMapper;
import com.sdlcplatform.repository.PasswordResetTokenRepository;
import com.sdlcplatform.repository.RefreshTokenRepository;
import com.sdlcplatform.repository.RoleRepository;
import com.sdlcplatform.repository.UserRepository;
import com.sdlcplatform.repository.VerificationTokenRepository;
import com.sdlcplatform.security.JwtService;
import com.sdlcplatform.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
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
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;
    @Mock private UserMapper userMapper;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getFrontend().setBaseUrl("http://localhost:5173");
        appProperties.getJwt().setAccessTokenExpirationMs(900000);
        appProperties.getJwt().setRefreshTokenExpirationMs(604800000);

        authService = new AuthServiceImpl(
                userRepository, roleRepository, refreshTokenRepository,
                verificationTokenRepository, passwordResetTokenRepository,
                passwordEncoder, authenticationManager, userDetailsService,
                jwtService, emailService, userMapper, appProperties);
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Jane Dev", "jane@sdlcplatform.com", "Password123", "Engineering");
        when(userRepository.existsByEmail("jane@sdlcplatform.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void register_shouldThrow_whenDefaultRoleMissing() {
        RegisterRequest request = new RegisterRequest("Jane Dev", "jane@sdlcplatform.com", "Password123", "Engineering");
        when(userRepository.existsByEmail("jane@sdlcplatform.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.DEVELOPER.name())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void register_shouldCreateUser_andSendVerificationEmail_onSuccess() {
        RegisterRequest request = new RegisterRequest("Jane Dev", "Jane@SdlcPlatform.com", "Password123", "Engineering");

        Role developerRole = Role.builder().name("DEVELOPER").build();
        User savedUser = User.builder()
                .fullName("Jane Dev")
                .email("jane@sdlcplatform.com")
                .passwordHash("hashed")
                .roles(Set.of(developerRole))
                .active(true)
                .emailVerified(false)
                .build();

        when(userRepository.existsByEmail("Jane@SdlcPlatform.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.DEVELOPER.name())).thenReturn(Optional.of(developerRole));
        when(passwordEncoder.encode("Password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(
                UserResponse.builder().id(UUID.randomUUID()).email("jane@sdlcplatform.com").build());

        UserResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("jane@sdlcplatform.com");
        verify(verificationTokenRepository).save(any());
        verify(emailService).sendVerificationEmail(any(), any(), any());
    }
}
