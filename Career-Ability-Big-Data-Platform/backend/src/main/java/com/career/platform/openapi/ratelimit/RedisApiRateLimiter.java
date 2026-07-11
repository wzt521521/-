package com.career.platform.openapi.ratelimit;

import java.time.Duration;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RedisApiRateLimiter implements ApiRateLimiter {

    private final StringRedisTemplate redisTemplate;

    public RedisApiRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitResult tryAcquire(Long apiKeyId, int limit) {
        long minute = Instant.now().getEpochSecond() / 60;
        String key = "ratelimit:" + apiKeyId + ":" + minute;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(2));
        }
        long actualCount = count == null ? limit + 1L : count;
        int remaining = (int) Math.max(0, limit - actualCount);
        return new RateLimitResult(actualCount <= limit, limit, remaining);
    }
}
