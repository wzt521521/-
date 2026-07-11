package com.career.platform.auth.service;

import com.career.platform.auth.dto.ChangePasswordRequest;
import com.career.platform.auth.dto.LoginRequest;
import com.career.platform.auth.dto.RegisterRequest;
import com.career.platform.auth.dto.TokenResponse;
import com.career.platform.auth.dto.UpdateProfileRequest;
import com.career.platform.auth.dto.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    void logout(String accessToken, String refreshToken);
}
