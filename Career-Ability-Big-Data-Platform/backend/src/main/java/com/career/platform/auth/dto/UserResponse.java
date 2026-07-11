package com.career.platform.auth.dto;

import com.career.platform.auth.entity.SysPermission;
import com.career.platform.auth.entity.SysRole;
import com.career.platform.auth.entity.SysUser;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserResponse {

    private final Long id;
    private final String username;
    private final String realName;
    private final String email;
    private final String phone;
    private final String college;
    private final Integer status;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final LocalDateTime createTime;

    public UserResponse(
            Long id,
            String username,
            String realName,
            String email,
            String phone,
            String college,
            Integer status,
            Set<String> roles,
            Set<String> permissions,
            LocalDateTime createTime) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.phone = phone;
        this.college = college;
        this.status = status;
        this.roles = roles;
        this.permissions = permissions;
        this.createTime = createTime;
    }

    public static UserResponse from(SysUser user) {
        Set<String> roleCodes = user.getRoles().stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> permissionCodes = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getCollege(),
                user.getStatus(),
                roleCodes,
                permissionCodes,
                user.getCreateTime());
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCollege() {
        return college;
    }

    public Integer getStatus() {
        return status;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}
