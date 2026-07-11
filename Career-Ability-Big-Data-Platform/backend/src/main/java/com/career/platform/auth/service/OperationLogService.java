package com.career.platform.auth.service;

import com.career.platform.auth.dto.OperationLogResponse;
import com.career.platform.common.PageResponse;

public interface OperationLogService {
    PageResponse<OperationLogResponse> list(String username, String module, int page, int size);

    OperationLogResponse get(Long id);
}
