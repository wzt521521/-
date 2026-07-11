package com.career.platform.openapi.service;

import com.career.platform.common.PageResponse;
import com.career.platform.openapi.dto.ApiCallLogResponse;
import com.career.platform.openapi.dto.ApiCallStatisticsResponse;
import com.career.platform.openapi.entity.ApiKey;

public interface ApiCallLogService {
    void record(
            ApiKey apiKey,
            String path,
            String method,
            String parameterNames,
            String ip,
            long duration,
            int statusCode);

    PageResponse<ApiCallLogResponse> list(String path, int page, int size);

    ApiCallStatisticsResponse statistics();
}
