package com.career.platform.openapi.dto;

import com.career.platform.openapi.entity.ApiKey;

public final class CreatedApiKeyResponse extends ApiKeyResponse {

    private final String apiKey;

    public CreatedApiKeyResponse(ApiKey entity, String apiKey) {
        super(entity);
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}
