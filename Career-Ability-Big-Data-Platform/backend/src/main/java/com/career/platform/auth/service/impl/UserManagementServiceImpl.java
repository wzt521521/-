package com.career.platform.auth.service.impl;

import com.career.platform.auth.dto.AdminCreateUserRequest;
import com.career.platform.auth.dto.AdminUpdateUserRequest;
import com.career.platform.auth.dto.ResetPasswordRequest;
import com.career.platform.auth.dto.UserResponse;
import com.career.platform.auth.entity.SysRole;
import com.career.platform.auth.entity.SysUser;
import com.career.platform.auth.repository.SysRoleRepository;
import com.career.platform.auth.repository.SysUserRepository;
import com.career.platform.auth.service.UserManagementService;
import com.career.platform.common.PageResponse;
import com.career.platform.common.error.BusinessException;
import com.career.platform.common.error.ErrorCode;
import com.career.platform.common.security.CurrentUserProvider;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    private static final String BOOTSTRAP_ADMIN = "admin";

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;

    public UserManagementServiceImpl(
            SysUserRepository userRepository,
            SysRoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<SysUser> users = userRepository.findByUsernameContainingIgnoreCase(
                keyword == null ? "" : keyword.trim(),
                pageRequest);
        List<UserResponse> content = users.getContent().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
        return PageResponse.from(users, content);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserResponse.from(requireUser(id));
    }

    @Override
    @Transactional
    public UserResponse create(AdminCreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Username already exists");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCollege(request.getCollege());
        user.setStatus(1);
        user.setRoles(resolveRoles(request.getRoleCodes()));
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, AdminUpdateUserRequest request) {
        SysUser user = requireUser(id);
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCollege(request.getCollege());
        user.setRoles(resolveRoles(request.getRoleCodes()));
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long id, int status) {
        SysUser user = requireUser(id);
        Long currentUserId = currentUserProvider.requireCurrentUser().getId();
        if (currentUserId.equals(id) && status == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Current user cannot disable itself");
        }
        if (BOOTSTRAP_ADMIN.equals(user.getUsername()) && status == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Bootstrap administrator cannot be disabled");
        }
        user.setStatus(status);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        SysUser user = requireUser(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysUser user = requireUser(id);
        if (currentUserProvider.requireCurrentUser().getId().equals(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Current user cannot delete itself");
        }
        if (BOOTSTRAP_ADMIN.equals(user.getUsername())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Bootstrap administrator cannot be deleted");
        }
        userRepository.delete(user);
    }

    private SysUser requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));
    }

    private Set<SysRole> resolveRoles(Set<String> roleCodes) {
        Set<SysRole> roles = new LinkedHashSet<>();
        for (String roleCode : roleCodes) {
            SysRole role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.BAD_REQUEST,
                            "Unknown role: " + roleCode));
            roles.add(role);
        }
        return roles;
    }
}
