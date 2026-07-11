package com.career.platform.auth.repository;

import com.career.platform.auth.entity.SysPermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {
    List<SysPermission> findAllByOrderBySortOrderAscIdAsc();
}
