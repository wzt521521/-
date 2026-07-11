package com.career.platform.auth.security;

import java.time.Duration;

public interface TokenStore {
    void storeRefreshToken(String tokenId, Long userId, Duration ttl);

    boolean consumeRefreshToken(String tokenId, Long userId);

    void revokeRefreshToken(String tokenId);

    void blacklistAccessToken(String tokenId, Duration ttl);

    boolean isAccessTokenBlacklisted(String tokenId);
}
