package com.career.platform.auth.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Size(min = 4, max = 20)
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "must contain only letters, numbers, or underscores")
    private String username;

    @NotBlank
    @Size(min = 6, max = 20)
    private String password;

    @Pattern(regexp = "^ROLE_(STUDENT|TEACHER)$", message = "must be ROLE_STUDENT or ROLE_TEACHER")
    private String roleCode;

    @Size(max = 50)
    private String realName;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
