package com.career.platform.auth.service.impl;

import com.career.platform.auth.dto.PermissionResponse;
import com.career.platform.auth.dto.RoleRequest;
import com.career.platform.auth.dto.RoleResponse;
import com.career.platform.auth.entity.SysPermission;
import com.career.platform.auth.entity.SysRole;
import com.career.platform.auth.repository.SysPermissionRepository;
import com.career.platform.auth.repository.SysRoleRepository;
import com.career.platform.auth.service.RoleManagementService;
import com.career.platform.common.error.BusinessException;
import com.career.platform.common.error.ErrorCode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final Set<String> BUILT_IN_ROLES = Set.of(
            "ROLE_ADMIN",
            "ROLE_ANALYST",
            "ROLE_TEACHER",
            "ROLE_COLLEGE_ADMIN",
            "ROLE_STUDENT");

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;

    public RoleManagementServiceImpl(
            SysRoleRepository roleRepository,
            SysPermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(SysRole::getId))
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRole(Long id) {
        return RoleResponse.from(requireRole(id));
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByRoleCode(request.getRoleCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Role code already exists");
        }
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        return RoleResponse.from(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        SysRole role = requireRole(id);
        if (BUILT_IN_ROLES.contains(role.getRoleCode())
                && !role.getRoleCode().equals(request.getRoleCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Built-in role code cannot be changed");
        }
        roleRepository.findByRoleCode(request.getRoleCode())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.CONFLICT, "Role code already exists");
                });
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        return RoleResponse.from(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        SysRole role = requireRole(id);
        if (BUILT_IN_ROLES.contains(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Built-in role cannot be deleted");
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> permissionTree() {
        List<SysPermission> permissions = permissionRepository.findAllByOrderBySortOrderAscIdAsc();
        Map<Long, PermissionResponse> nodes = new HashMap<>();
        permissions.forEach(permission -> nodes.put(permission.getId(), PermissionResponse.from(permission)));
        List<PermissionResponse> roots = new ArrayList<>();
        for (SysPermission permission : permissions) {
            PermissionResponse node = nodes.get(permission.getId());
            if (permission.getParentId() == null
                    || permission.getParentId() == 0L
                    || !nodes.containsKey(permission.getParentId())) {
                roots.add(node);
            } else {
                nodes.get(permission.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getRolePermissionIds(Long roleId) {
        return requireRole(roleId).getPermissions().stream()
                .map(SysPermission::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public RoleResponse replaceRolePermissions(Long roleId, Set<Long> permissionIds) {
        SysRole role = requireRole(roleId);
        List<SysPermission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "One or more permissions do not exist");
        }
        role.setPermissions(new LinkedHashSet<>(permissions));
        return RoleResponse.from(roleRepository.save(role));
    }

    private SysRole requireRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Role not found"));
    }
}
