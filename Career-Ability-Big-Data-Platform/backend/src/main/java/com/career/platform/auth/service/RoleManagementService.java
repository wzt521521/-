package com.career.platform.auth.service;

import com.career.platform.auth.dto.PermissionResponse;
import com.career.platform.auth.dto.RoleRequest;
import com.career.platform.auth.dto.RoleResponse;
import java.util.List;
import java.util.Set;

public interface RoleManagementService {
    List<RoleResponse> listRoles();

    RoleResponse getRole(Long id);

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);

    void deleteRole(Long id);

    List<PermissionResponse> permissionTree();

    Set<Long> getRolePermissionIds(Long roleId);

    RoleResponse replaceRolePermissions(Long roleId, Set<Long> permissionIds);
}
