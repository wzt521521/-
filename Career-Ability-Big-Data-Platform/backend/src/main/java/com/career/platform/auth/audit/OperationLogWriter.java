package com.career.platform.auth.audit;

import com.career.platform.auth.entity.SysOperationLog;
import com.career.platform.auth.repository.SysOperationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationLogWriter {

    private final SysOperationLogRepository logRepository;

    public OperationLogWriter(SysOperationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(SysOperationLog operationLog) {
        logRepository.save(operationLog);
    }
}
