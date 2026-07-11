package com.career.platform.auth.security;

public interface LoginAttemptService {
    boolean isBlocked(String username);

    void recordFailure(String username);

    void recordSuccess(String username);
}
