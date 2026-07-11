package com.career.platform.auth.repository;

import com.career.platform.auth.entity.SysRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysRoleRepository extends JpaRepository<SysRole, Long> {
    Optional<SysRole> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);
}
