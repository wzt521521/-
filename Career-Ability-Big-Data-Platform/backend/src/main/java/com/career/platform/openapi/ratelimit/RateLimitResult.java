package com.career.platform.openapi.ratelimit;

public final class RateLimitResult {

    private final boolean allowed;
    private final int limit;
    private final int remaining;

    public RateLimitResult(boolean allowed, int limit, int remaining) {
        this.allowed = allowed;
        this.limit = limit;
        this.remaining = remaining;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public int getLimit() {
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }
}
