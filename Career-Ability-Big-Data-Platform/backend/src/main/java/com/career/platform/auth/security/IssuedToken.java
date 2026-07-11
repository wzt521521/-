package com.career.platform.auth.security;

import java.time.Instant;

public final class IssuedToken {

    private final String value;
    private final String id;
    private final Instant expiresAt;

    public IssuedToken(String value, String id, Instant expiresAt) {
        this.value = value;
        this.id = id;
        this.expiresAt = expiresAt;
    }

    public String getValue() {
        return value;
    }

    public String getId() {
        return id;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
