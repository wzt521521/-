package com.career.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.career.platform.auth.security.RoleBasedDataScopeProvider;
import com.career.platform.common.error.BusinessException;
import com.career.platform.common.security.CurrentUser;
import com.career.platform.common.security.DataScope;
import com.career.platform.common.security.DataScopeType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RoleBasedDataScopeProviderTest {

    @Test
    void grantsAllScopeToAdministratorsAndAnalysts() {
        DataScope adminScope = provider(user(1L, null, "ROLE_ADMIN")).requireDataScope();
        DataScope analystScope = provider(user(2L, null, "ROLE_ANALYST")).requireDataScope();

        assertTrue(adminScope.isAll());
        assertEquals(DataScopeType.ALL, analystScope.getType());
    }

    @Test
    void limitsTeachersAndCollegeAdministratorsToTheirCollege() {
        DataScope scope = provider(user(3L, "计算机学院", "ROLE_COLLEGE_ADMIN"))
                .requireDataScope();

        assertEquals(DataScopeType.COLLEGE, scope.getType());
        assertEquals("计算机学院", scope.getCollege());
    }

    @Test
    void limitsStudentsAndUnknownRolesToTheirOwnRecords() {
        DataScope scope = provider(user(4L, "计算机学院", "ROLE_STUDENT"))
                .requireDataScope();

        assertEquals(DataScopeType.SELF, scope.getType());
        assertEquals(4L, scope.getUserId());
    }

    @Test
    void rejectsCollegeScopeWithoutAnAssignedCollege() {
        assertThrows(
                BusinessException.class,
                () -> provider(user(5L, null, "ROLE_TEACHER")).requireDataScope());
    }

    private RoleBasedDataScopeProvider provider(CurrentUser currentUser) {
        return new RoleBasedDataScopeProvider(() -> currentUser);
    }

    private CurrentUser user(Long id, String college, String... roles) {
        return new CurrentUser(id, "user" + id, college, Set.of(roles), Set.of());
    }
}
