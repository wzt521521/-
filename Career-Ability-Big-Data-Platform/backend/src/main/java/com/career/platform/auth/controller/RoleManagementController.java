package com.career.platform.auth.controller;

import com.career.platform.auth.dto.PermissionResponse;
import com.career.platform.auth.dto.RolePermissionsRequest;
import com.career.platform.auth.dto.RoleRequest;
import com.career.platform.auth.dto.RoleResponse;
import com.career.platform.auth.service.RoleManagementService;
import com.career.platform.common.annotation.Log;
import com.career.platform.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "角色与权限", description = "角色维护、权限树查询和权限分配")
@SecurityRequirement(name = "bearerAuth")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @GetMapping("/roles")
    @Operation(summary = "查询角色列表")
    @PreAuthorize("hasAuthority('role:read')")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.success(roleManagementService.listRoles());
    }

    @GetMapping("/roles/{id}")
    @Operation(summary = "查询角色详情")
    @PreAuthorize("hasAuthority('role:read')")
    public ApiResponse<RoleResponse> getRole(@PathVariable Long id) {
        return ApiResponse.success(roleManagementService.getRole(id));
    }

    @PostMapping("/roles")
    @Operation(summary = "创建角色")
    @PreAuthorize("hasAuthority('role:update')")
    @Log(module = "role", operation = "create", description = "Create system role")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleManagementService.createRole(request));
    }

    @PutMapping("/roles/{id}")
    @Operation(summary = "修改角色")
    @PreAuthorize("hasAuthority('role:update')")
    @Log(module = "role", operation = "update", description = "Update system role")
    public ApiResponse<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleManagementService.updateRole(id, request));
    }

    @DeleteMapping("/roles/{id}")
    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('role:update')")
    @Log(module = "role", operation = "delete", description = "Delete system role")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleManagementService.deleteRole(id);
        return ApiResponse.success();
    }

    @GetMapping("/permissions/tree")
    @Operation(summary = "查询权限树")
    @PreAuthorize("hasAuthority('role:read')")
    public ApiResponse<List<PermissionResponse>> permissionTree() {
        return ApiResponse.success(roleManagementService.permissionTree());
    }

    @GetMapping("/roles/{id}/permissions")
    @Operation(summary = "查询角色权限 ID")
    @PreAuthorize("hasAuthority('role:read')")
    public ApiResponse<Set<Long>> rolePermissions(@PathVariable Long id) {
        return ApiResponse.success(roleManagementService.getRolePermissionIds(id));
    }

    @PutMapping("/roles/{id}/permissions")
    @Operation(summary = "替换角色权限")
    @PreAuthorize("hasAuthority('role:update')")
    @Log(module = "role", operation = "permissions", description = "Replace role permissions")
    public ApiResponse<RoleResponse> replaceRolePermissions(
            @PathVariable Long id,
            @Valid @RequestBody RolePermissionsRequest request) {
        return ApiResponse.success(
                roleManagementService.replaceRolePermissions(id, request.getPermissionIds()));
    }
}
