package com.sdlcplatform.controller;

import com.sdlcplatform.dto.request.ChangePasswordRequest;
import com.sdlcplatform.dto.request.ForgotPasswordRequest;
import com.sdlcplatform.dto.request.LoginRequest;
import com.sdlcplatform.dto.request.RefreshTokenRequest;
import com.sdlcplatform.dto.request.RegisterRequest;
import com.sdlcplatform.dto.request.ResetPasswordRequest;
import com.sdlcplatform.dto.response.ApiResponse;
import com.sdlcplatform.dto.response.AuthResponse;
import com.sdlcplatform.dto.response.UserResponse;
import com.sdlcplatform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, tokens, and password/email flows")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates an account with the default DEVELOPER role and sends a verification email")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Returns an access token and refresh token pair for a verified, active account")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Exchanges a valid, non-revoked refresh token for a new token pair (rotation)")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out", description = "Revokes the given refresh token so it can no longer be used")
    public ResponseEntity<ApiResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.of("Logged out successfully"));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Confirms the account's email address using the token emailed at registration")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.of("Email verified successfully. You can now log in."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset", description = "Always returns success to avoid revealing whether an email is registered")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.of("If that email is registered, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Sets a new password using a valid reset token and revokes all existing sessions")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.of("Password has been reset. Please log in again."));
    }

    @PatchMapping("/change-password")
    @Operation(summary = "Change password", description = "Requires the current password; revokes all existing refresh tokens on success")
    public ResponseEntity<ApiResponse> changePassword(@Parameter(hidden = true) Authentication authentication,
                                                      @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.of("Password changed successfully"));
    }
}