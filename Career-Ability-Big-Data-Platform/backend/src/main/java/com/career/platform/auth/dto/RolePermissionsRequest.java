package com.career.platform.auth.dto;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;

public class RolePermissionsRequest {

    @NotNull
    private Set<Long> permissionIds = new LinkedHashSet<>();

    public Set<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(Set<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
