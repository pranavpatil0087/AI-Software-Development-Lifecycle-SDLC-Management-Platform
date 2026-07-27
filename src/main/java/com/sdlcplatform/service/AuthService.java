package com.sdlcplatform.service;

import com.sdlcplatform.dto.request.ChangePasswordRequest;
import com.sdlcplatform.dto.request.ForgotPasswordRequest;
import com.sdlcplatform.dto.request.LoginRequest;
import com.sdlcplatform.dto.request.RegisterRequest;
import com.sdlcplatform.dto.request.ResetPasswordRequest;
import com.sdlcplatform.dto.response.AuthResponse;
import com.sdlcplatform.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    void verifyEmail(String token);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(String currentUserEmail, ChangePasswordRequest request);
}
