package com.sdlcplatform.service.impl;

import com.sdlcplatform.config.AppProperties;
import com.sdlcplatform.dto.request.ChangePasswordRequest;
import com.sdlcplatform.dto.request.ForgotPasswordRequest;
import com.sdlcplatform.dto.request.LoginRequest;
import com.sdlcplatform.dto.request.RegisterRequest;
import com.sdlcplatform.dto.request.ResetPasswordRequest;
import com.sdlcplatform.dto.response.AuthResponse;
import com.sdlcplatform.dto.response.UserResponse;
import com.sdlcplatform.entity.PasswordResetToken;
import com.sdlcplatform.entity.RefreshToken;
import com.sdlcplatform.entity.Role;
import com.sdlcplatform.entity.User;
import com.sdlcplatform.entity.VerificationToken;
import com.sdlcplatform.exception.AccountDeactivatedException;
import com.sdlcplatform.exception.AccountNotVerifiedException;
import com.sdlcplatform.exception.EmailAlreadyExistsException;
import com.sdlcplatform.exception.InvalidCredentialsException;
import com.sdlcplatform.exception.InvalidTokenException;
import com.sdlcplatform.exception.ResourceNotFoundException;
import com.sdlcplatform.mapper.UserMapper;
import com.sdlcplatform.repository.PasswordResetTokenRepository;
import com.sdlcplatform.repository.RefreshTokenRepository;
import com.sdlcplatform.repository.RoleRepository;
import com.sdlcplatform.repository.UserRepository;
import com.sdlcplatform.repository.VerificationTokenRepository;
import com.sdlcplatform.security.JwtService;
import com.sdlcplatform.service.AuthService;
import com.sdlcplatform.service.EmailService;
import com.sdlcplatform.util.HashUtil;
import com.sdlcplatform.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final long VERIFICATION_TOKEN_TTL_HOURS = 24;
    private static final long RESET_TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        Role developerRole = roleRepository.findByName(Role.RoleName.DEVELOPER.name())
                .orElseThrow(() -> new ResourceNotFoundException("Default role DEVELOPER not found — has V2 migration run?"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .department(request.getDepartment())
                .active(true)
                .emailVerified(false)
                .roles(Set.of(developerRole))
                .build();

        User saved = userRepository.save(user);

        issueVerificationToken(saved);

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new AccountDeactivatedException("This account has been deactivated. Contact an administrator.");
        }
        if (!user.isEmailVerified()) {
            throw new AccountNotVerifiedException("Please verify your email before logging in.");
        }

        // Delegates to Spring Security's DaoAuthenticationProvider, which itself
        // uses the configured PasswordEncoder — never compare raw hashes manually.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return buildAuthResponse(user, userDetails);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String rawRefreshToken) {
        String tokenHash = HashUtil.sha256(rawRefreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired or been revoked. Please log in again.");
        }

        User user = storedToken.getUser();
        if (!user.isActive()) {
            throw new AccountDeactivatedException("This account has been deactivated.");
        }

        // Rotate: revoke the old refresh token and issue a brand new pair.
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return buildAuthResponse(user, userDetails);
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = HashUtil.sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Verification link is invalid"));

        if (verificationToken.isUsed() || verificationToken.isExpired()) {
            throw new InvalidTokenException("Verification link has expired or already been used");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Deliberately do not reveal whether the email exists — avoids account enumeration.
        userRepository.findByEmail(request.getEmail().toLowerCase()).ifPresent(user -> {
            String rawToken = TokenGenerator.generate();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(rawToken)
                    .expiryDate(Instant.now().plus(RESET_TOKEN_TTL_MINUTES, ChronoUnit.MINUTES))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String resetLink = appProperties.getFrontend().getBaseUrl() + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Password reset link is invalid"));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new InvalidTokenException("Password reset link has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Invalidate every existing session — a password reset should force re-login everywhere.
        refreshTokenRepository.revokeAllForUser(user.getId());
    }

    @Override
    @Transactional
    public void changePassword(String currentUserEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllForUser(user.getId());
    }

    private void issueVerificationToken(User user) {
        String rawToken = TokenGenerator.generate();

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(rawToken)
                .expiryDate(Instant.now().plus(VERIFICATION_TOKEN_TTL_HOURS, ChronoUnit.HOURS))
                .used(false)
                .build();
        verificationTokenRepository.save(verificationToken);

        String verificationLink = appProperties.getFrontend().getBaseUrl() + "/verify-email?token=" + rawToken;
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationLink);
    }

    private AuthResponse buildAuthResponse(User user, UserDetails userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);

        String rawRefreshToken = TokenGenerator.generate();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(HashUtil.sha256(rawRefreshToken))
                .expiryDate(Instant.now().plus(appProperties.getJwt().getRefreshTokenExpirationMs(), ChronoUnit.MILLIS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(appProperties.getJwt().getAccessTokenExpirationMs())
                .user(userMapper.toResponse(user))
                .build();
    }
}
