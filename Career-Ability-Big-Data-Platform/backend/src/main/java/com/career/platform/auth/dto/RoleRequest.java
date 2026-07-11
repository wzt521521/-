package com.career.platform.auth.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RoleRequest {

    @NotBlank
    @Size(max = 50)
    private String roleName;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^ROLE_[A-Z0-9_]+$")
    private String roleCode;

    @Size(max = 200)
    private String description;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
