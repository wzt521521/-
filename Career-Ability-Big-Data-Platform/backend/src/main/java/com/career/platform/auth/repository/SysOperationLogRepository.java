package com.career.platform.auth.repository;

import com.career.platform.auth.entity.SysOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysOperationLogRepository
        extends JpaRepository<SysOperationLog, Long>, JpaSpecificationExecutor<SysOperationLog> {
    Page<SysOperationLog> findByUsernameContainingIgnoreCaseAndModuleContainingIgnoreCase(
            String username,
            String module,
            Pageable pageable);
}
