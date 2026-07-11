package com.career.platform.auth.security;

import com.career.platform.auth.entity.SysPermission;
import com.career.platform.auth.entity.SysRole;
import com.career.platform.auth.entity.SysUser;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String college;
    private final boolean enabled;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Set<GrantedAuthority> authorities;

    private CustomUserPrincipal(
            Long id,
            String username,
            String password,
            String college,
            boolean enabled,
            Set<String> roles,
            Set<String> permissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.college = college;
        this.enabled = enabled;
        this.roles = Set.copyOf(roles);
        this.permissions = Set.copyOf(permissions);
        this.authorities = new LinkedHashSet<>();
        roles.stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
        permissions.stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
    }

    public static CustomUserPrincipal from(SysUser user) {
        Set<String> roleCodes = user.getRoles().stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> permissionCodes = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new CustomUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getCollege(),
                Integer.valueOf(1).equals(user.getStatus()),
                roleCodes,
                permissionCodes);
    }

    public Long getId() {
        return id;
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
