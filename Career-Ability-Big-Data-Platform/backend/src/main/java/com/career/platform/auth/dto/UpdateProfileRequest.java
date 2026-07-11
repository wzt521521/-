package com.career.platform.auth.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 50)
    private String realName;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

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
