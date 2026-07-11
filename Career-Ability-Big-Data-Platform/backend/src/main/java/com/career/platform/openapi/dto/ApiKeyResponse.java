package com.career.platform.openapi.dto;

import com.career.platform.openapi.entity.ApiKey;
import java.time.LocalDateTime;

public class ApiKeyResponse {

    private final Long id;
    private final Long userId;
    private final String appName;
    private final Integer status;
    private final Integer rateLimit;
    private final Long totalCalls;
    private final LocalDateTime expireTime;
    private final LocalDateTime createTime;

    protected ApiKeyResponse(ApiKey apiKey) {
        this.id = apiKey.getId();
        this.userId = apiKey.getUserId();
        this.appName = apiKey.getAppName();
        this.status = apiKey.getStatus();
        this.rateLimit = apiKey.getRateLimit();
        this.totalCalls = apiKey.getTotalCalls();
        this.expireTime = apiKey.getExpireTime();
        this.createTime = apiKey.getCreateTime();
    }

    public static ApiKeyResponse from(ApiKey apiKey) {
        return new ApiKeyResponse(apiKey);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAppName() {
        return appName;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getRateLimit() {
        return rateLimit;
    }

    public Long getTotalCalls() {
        return totalCalls;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}
