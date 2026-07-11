package com.career.platform.auth.dto;

import com.career.platform.auth.entity.SysPermission;
import com.career.platform.auth.entity.SysRole;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleResponse {

    private final Long id;
    private final String roleName;
    private final String roleCode;
    private final String description;
    private final Set<Long> permissionIds;
    private final Set<String> permissionCodes;
    private final LocalDateTime createTime;

    private RoleResponse(
            Long id,
            String roleName,
            String roleCode,
            String description,
            Set<Long> permissionIds,
            Set<String> permissionCodes,
            LocalDateTime createTime) {
        this.id = id;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.description = description;
        this.permissionIds = permissionIds;
        this.permissionCodes = permissionCodes;
        this.createTime = createTime;
    }

    public static RoleResponse from(SysRole role) {
        return new RoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getRoleCode(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(SysPermission::getId)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                role.getPermissions().stream()
                        .map(SysPermission::getPermissionCode)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                role.getCreateTime());
    }

    public Long getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getDescription() {
        return description;
    }

    public Set<Long> getPermissionIds() {
        return permissionIds;
    }

    public Set<String> getPermissionCodes() {
        return permissionCodes;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}
