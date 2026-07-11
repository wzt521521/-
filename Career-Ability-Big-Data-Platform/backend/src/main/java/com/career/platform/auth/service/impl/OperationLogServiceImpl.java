package com.career.platform.auth.service.impl;

import com.career.platform.auth.dto.OperationLogResponse;
import com.career.platform.auth.entity.SysOperationLog;
import com.career.platform.auth.repository.SysOperationLogRepository;
import com.career.platform.auth.service.OperationLogService;
import com.career.platform.common.PageResponse;
import com.career.platform.common.error.BusinessException;
import com.career.platform.common.error.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final SysOperationLogRepository logRepository;

    public OperationLogServiceImpl(SysOperationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OperationLogResponse> list(
            String username,
            String module,
            int page,
            int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SysOperationLog> logs = logRepository
                .findByUsernameContainingIgnoreCaseAndModuleContainingIgnoreCase(
                        username == null ? "" : username.trim(),
                        module == null ? "" : module.trim(),
                        pageRequest);
        List<OperationLogResponse> content = logs.getContent().stream()
                .map(OperationLogResponse::from)
                .collect(Collectors.toList());
        return PageResponse.from(logs, content);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationLogResponse get(Long id) {
        return logRepository.findById(id)
                .map(OperationLogResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Operation log not found"));
    }
}
