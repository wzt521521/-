package com.career.platform.auth.security;

import java.time.Duration;
import java.util.Collections;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class RedisTokenStore implements TokenStore {

    private static final String REFRESH_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";
    private static final DefaultRedisScript<Long> CONSUME_REFRESH_TOKEN =
            new DefaultRedisScript<>(
                    "local value = redis.call('GET', KEYS[1]); "
                            + "if value == ARGV[1] then redis.call('DEL', KEYS[1]); return 1; end; "
                            + "return 0;",
                    Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void storeRefreshToken(String tokenId, Long userId, Duration ttl) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + tokenId, userId.toString(), ttl);
    }

    @Override
    public boolean consumeRefreshToken(String tokenId, Long userId) {
        Long consumed = redisTemplate.execute(
                CONSUME_REFRESH_TOKEN,
                Collections.singletonList(REFRESH_PREFIX + tokenId),
                userId.toString());
        return Long.valueOf(1L).equals(consumed);
    }

    @Override
    public void revokeRefreshToken(String tokenId) {
        redisTemplate.delete(REFRESH_PREFIX + tokenId);
    }

    @Override
    public void blacklistAccessToken(String tokenId, Duration ttl) {
        if (!ttl.isZero() && !ttl.isNegative()) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + tokenId, "1", ttl);
        }
    }

    @Override
    public boolean isAccessTokenBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + tokenId));
    }
}
