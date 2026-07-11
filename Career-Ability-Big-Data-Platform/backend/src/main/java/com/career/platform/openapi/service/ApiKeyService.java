package com.career.platform.openapi.service;

import com.career.platform.common.PageResponse;
import com.career.platform.openapi.dto.ApiKeyResponse;
import com.career.platform.openapi.dto.CreateApiKeyRequest;
import com.career.platform.openapi.dto.CreatedApiKeyResponse;
import com.career.platform.openapi.entity.ApiKey;

public interface ApiKeyService {
    PageResponse<ApiKeyResponse> list(String appName, int page, int size);

    CreatedApiKeyResponse create(CreateApiKeyRequest request);

    ApiKeyResponse updateStatus(Long id, int status);

    void delete(Long id);

    ApiKey authenticate(String rawApiKey);
}
