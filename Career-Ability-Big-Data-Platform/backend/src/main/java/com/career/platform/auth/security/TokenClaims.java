package com.career.platform.auth.security;

import java.time.Instant;

public final class TokenClaims {

    private final Long userId;
    private final String username;
    private final String tokenId;
    private final String tokenType;
    private final Instant expiresAt;

    public TokenClaims(
            Long userId,
            String username,
            String tokenId,
            String tokenType,
            Instant expiresAt) {
        this.userId = userId;
        this.username = username;
        this.tokenId = tokenId;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
