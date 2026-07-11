package com.career.platform.openapi.ratelimit;

public interface ApiRateLimiter {
    RateLimitResult tryAcquire(Long apiKeyId, int limit);
}
