package com.career.platform.openapi.dto;

import com.career.platform.openapi.entity.ApiCallLog;
import java.time.LocalDateTime;

public final class ApiCallLogResponse {

    private final Long id;
    private final Long apiKeyId;
    private final String apiPath;
    private final String method;
    private final String params;
    private final String ip;
    private final Long duration;
    private final Integer statusCode;
    private final LocalDateTime createTime;

    private ApiCallLogResponse(ApiCallLog log) {
        this.id = log.getId();
        this.apiKeyId = log.getApiKeyId();
        this.apiPath = log.getApiPath();
        this.method = log.getMethod();
        this.params = log.getParams();
        this.ip = log.getIp();
        this.duration = log.getDuration();
        this.statusCode = log.getStatusCode();
        this.createTime = log.getCreateTime();
    }

    public static ApiCallLogResponse from(ApiCallLog log) {
        return new ApiCallLogResponse(log);
    }

    public Long getId() {
        return id;
    }

    public Long getApiKeyId() {
        return apiKeyId;
    }

    public String getApiPath() {
        return apiPath;
    }

    public String getMethod() {
        return method;
    }

    public String getParams() {
        return params;
    }

    public String getIp() {
        return ip;
    }

    public Long getDuration() {
        return duration;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}
