package com.career.platform.auth.security;

import com.career.platform.common.error.BusinessException;
import com.career.platform.common.error.ErrorCode;
import com.career.platform.common.security.CurrentUser;
import com.career.platform.common.security.CurrentUserProvider;
import com.career.platform.common.security.DataScope;
import com.career.platform.common.security.DataScopeProvider;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RoleBasedDataScopeProvider implements DataScopeProvider {

    private static final Set<String> ALL_DATA_ROLES = Set.of("ROLE_ADMIN", "ROLE_ANALYST");
    private static final Set<String> COLLEGE_DATA_ROLES =
            Set.of("ROLE_COLLEGE_ADMIN", "ROLE_TEACHER");

    private final CurrentUserProvider currentUserProvider;

    public RoleBasedDataScopeProvider(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public DataScope requireDataScope() {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        if (currentUser.getRoles().stream().anyMatch(ALL_DATA_ROLES::contains)) {
            return DataScope.all();
        }
        if (currentUser.getRoles().stream().anyMatch(COLLEGE_DATA_ROLES::contains)) {
            if (!StringUtils.hasText(currentUser.getCollege())) {
                throw new BusinessException(
                        ErrorCode.FORBIDDEN,
                        "College data scope requires an assigned college");
            }
            return DataScope.college(currentUser.getCollege());
        }
        return DataScope.self(currentUser.getId());
    }
}
