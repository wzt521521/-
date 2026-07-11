package com.career.platform.auth.service;

import com.career.platform.auth.dto.AdminCreateUserRequest;
import com.career.platform.auth.dto.AdminUpdateUserRequest;
import com.career.platform.auth.dto.ResetPasswordRequest;
import com.career.platform.auth.dto.UserResponse;
import com.career.platform.common.PageResponse;

public interface UserManagementService {
    PageResponse<UserResponse> list(String keyword, int page, int size);

    UserResponse get(Long id);

    UserResponse create(AdminCreateUserRequest request);

    UserResponse update(Long id, AdminUpdateUserRequest request);

    UserResponse updateStatus(Long id, int status);

    void resetPassword(Long id, ResetPasswordRequest request);

    void delete(Long id);
}
