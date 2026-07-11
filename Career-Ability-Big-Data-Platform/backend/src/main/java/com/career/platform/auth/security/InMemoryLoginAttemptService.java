package com.career.platform.auth.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryLoginAttemptService implements LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();
    private final Map<String, Instant> locks = new ConcurrentHashMap<>();

    @Override
    public boolean isBlocked(String username) {
        String key = normalize(username);
        Instant lockUntil = locks.get(key);
        if (lockUntil == null) {
            return false;
        }
        if (lockUntil.isBefore(Instant.now())) {
            locks.remove(key);
            return false;
        }
        return true;
    }

    @Override
    public synchronized void recordFailure(String username) {
        String key = normalize(username);
        Instant now = Instant.now();
        Deque<Instant> attempts = failures.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(now.minus(WINDOW))) {
            attempts.removeFirst();
        }
        attempts.addLast(now);
        if (attempts.size() >= MAX_FAILURES) {
            locks.put(key, now.plus(LOCK_DURATION));
        }
    }

    @Override
    public void recordSuccess(String username) {
        String key = normalize(username);
        failures.remove(key);
        locks.remove(key);
    }

    private String normalize(String username) {
        return username.toLowerCase();
    }
}
