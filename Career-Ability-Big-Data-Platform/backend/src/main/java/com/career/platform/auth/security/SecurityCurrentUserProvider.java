package com.career.platform.auth.security;

import com.career.platform.common.error.BusinessException;
import com.career.platform.common.error.ErrorCode;
import com.career.platform.common.security.CurrentUser;
import com.career.platform.common.security.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserPrincipal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        return new CurrentUser(
                principal.getId(),
                principal.getUsername(),
                principal.getCollege(),
                principal.getRoles(),
                principal.getPermissions());
    }
}
