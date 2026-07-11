package com.career.platform.auth.security;

import java.time.Duration;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RedisLoginAttemptService implements LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final String FAILURE_PREFIX = "login:fail:";
    private static final String LOCK_PREFIX = "login:lock:";

    private final StringRedisTemplate redisTemplate;

    public RedisLoginAttemptService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isBlocked(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey(username)));
    }

    @Override
    public void recordFailure(String username) {
        long now = System.currentTimeMillis();
        String key = failureKey(username);
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, now - WINDOW.toMillis());
        redisTemplate.opsForZSet().add(key, now + ":" + UUID.randomUUID(), now);
        redisTemplate.expire(key, WINDOW.plus(LOCK_DURATION));
        Long failures = redisTemplate.opsForZSet().zCard(key);
        if (failures != null && failures >= MAX_FAILURES) {
            redisTemplate.opsForValue().set(lockKey(username), "1", LOCK_DURATION);
        }
    }

    @Override
    public void recordSuccess(String username) {
        redisTemplate.delete(failureKey(username));
        redisTemplate.delete(lockKey(username));
    }

    private String failureKey(String username) {
        return FAILURE_PREFIX + username.toLowerCase();
    }

    private String lockKey(String username) {
        return LOCK_PREFIX + username.toLowerCase();
    }
}
