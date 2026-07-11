package com.career.platform.auth.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryTokenStore implements TokenStore {

    private final Map<String, ExpiringValue<Long>> refreshTokens = new ConcurrentHashMap<>();
    private final Map<String, Instant> accessBlacklist = new ConcurrentHashMap<>();

    @Override
    public void storeRefreshToken(String tokenId, Long userId, Duration ttl) {
        refreshTokens.put(tokenId, new ExpiringValue<>(userId, Instant.now().plus(ttl)));
    }

    @Override
    public synchronized boolean consumeRefreshToken(String tokenId, Long userId) {
        ExpiringValue<Long> value = refreshTokens.get(tokenId);
        if (value == null || value.expiresAt.isBefore(Instant.now())) {
            refreshTokens.remove(tokenId);
            return false;
        }
        if (!userId.equals(value.value)) {
            return false;
        }
        refreshTokens.remove(tokenId);
        return true;
    }

    @Override
    public void revokeRefreshToken(String tokenId) {
        refreshTokens.remove(tokenId);
    }

    @Override
    public void blacklistAccessToken(String tokenId, Duration ttl) {
        accessBlacklist.put(tokenId, Instant.now().plus(ttl));
    }

    @Override
    public boolean isAccessTokenBlacklisted(String tokenId) {
        Instant expiresAt = accessBlacklist.get(tokenId);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            accessBlacklist.remove(tokenId);
            return false;
        }
        return true;
    }

    private static final class ExpiringValue<T> {
        private final T value;
        private final Instant expiresAt;

        private ExpiringValue(T value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
