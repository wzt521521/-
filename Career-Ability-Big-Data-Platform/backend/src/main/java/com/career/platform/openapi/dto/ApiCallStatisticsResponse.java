package com.career.platform.openapi.dto;

public final class ApiCallStatisticsResponse {

    private final long totalCalls;
    private final long successfulCalls;
    private final long failedCalls;
    private final double averageDuration;

    public ApiCallStatisticsResponse(
            long totalCalls,
            long successfulCalls,
            long failedCalls,
            double averageDuration) {
        this.totalCalls = totalCalls;
        this.successfulCalls = successfulCalls;
        this.failedCalls = failedCalls;
        this.averageDuration = averageDuration;
    }

    public long getTotalCalls() {
        return totalCalls;
    }

    public long getSuccessfulCalls() {
        return successfulCalls;
    }

    public long getFailedCalls() {
        return failedCalls;
    }

    public double getAverageDuration() {
        return averageDuration;
    }
}
