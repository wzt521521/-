package com.career.platform.openapi.ratelimit;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryApiRateLimiter implements ApiRateLimiter {

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @Override
    public RateLimitResult tryAcquire(Long apiKeyId, int limit) {
        long minute = Instant.now().getEpochSecond() / 60;
        String key = apiKeyId + ":" + minute;
        int count = counters.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet();
        return new RateLimitResult(count <= limit, limit, Math.max(0, limit - count));
    }
}
