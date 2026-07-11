package com.career.platform.common.security;

import java.util.Collections;
import java.util.Set;

public final class CurrentUser {

    private final Long id;
    private final String username;
    private final String college;
    private final Set<String> roles;
    private final Set<String> permissions;

    public CurrentUser(
            Long id,
            String username,
            String college,
            Set<String> roles,
            Set<String> permissions) {
        this.id = id;
        this.username = username;
        this.college = college;
        this.roles = roles == null ? Collections.emptySet() : Set.copyOf(roles);
        this.permissions = permissions == null ? Collections.emptySet() : Set.copyOf(permissions);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getCollege() {
        return college;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}
