package com.career.platform.auth.dto;

import com.career.platform.auth.entity.SysOperationLog;
import java.time.LocalDateTime;

public final class OperationLogResponse {

    private final Long id;
    private final Long userId;
    private final String username;
    private final String module;
    private final String operation;
    private final String description;
    private final String method;
    private final String params;
    private final String ip;
    private final Long duration;
    private final Integer status;
    private final String errorMessage;
    private final LocalDateTime createTime;

    private OperationLogResponse(SysOperationLog log) {
        this.id = log.getId();
        this.userId = log.getUserId();
        this.username = log.getUsername();
        this.module = log.getModule();
        this.operation = log.getOperation();
        this.description = log.getDescription();
        this.method = log.getMethod();
        this.params = log.getParams();
        this.ip = log.getIp();
        this.duration = log.getDuration();
        this.status = log.getStatus();
        this.errorMessage = log.getErrorMessage();
        this.createTime = log.getCreateTime();
    }

    public static OperationLogResponse from(SysOperationLog log) {
        return new OperationLogResponse(log);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getModule() {
        return module;
    }

    public String getOperation() {
        return operation;
    }

    public String getDescription() {
        return description;
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

    public Integer getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}
