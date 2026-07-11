package com.career.platform.auth.dto;

import com.career.platform.auth.entity.SysPermission;
import java.util.ArrayList;
import java.util.List;

public class PermissionResponse {

    private final Long id;
    private final String permissionName;
    private final String permissionCode;
    private final Long parentId;
    private final String type;
    private final String path;
    private final String icon;
    private final Integer sortOrder;
    private final List<PermissionResponse> children = new ArrayList<>();

    private PermissionResponse(SysPermission permission) {
        this.id = permission.getId();
        this.permissionName = permission.getPermissionName();
        this.permissionCode = permission.getPermissionCode();
        this.parentId = permission.getParentId();
        this.type = permission.getType();
        this.path = permission.getPath();
        this.icon = permission.getIcon();
        this.sortOrder = permission.getSortOrder();
    }

    public static PermissionResponse from(SysPermission permission) {
        return new PermissionResponse(permission);
    }

    public Long getId() {
        return id;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String getIcon() {
        return icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public List<PermissionResponse> getChildren() {
        return children;
    }
}
