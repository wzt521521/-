package com.career.platform.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class TokenStoreConcurrencyTest {

    @Test
    void refreshTokenCanBeConsumedOnlyOnce() {
        InMemoryTokenStore tokenStore = new InMemoryTokenStore();
        tokenStore.storeRefreshToken("refresh-id", 7L, Duration.ofMinutes(1));

        long successfulConsumers = IntStream.range(0, 32)
                .parallel()
                .filter(ignored -> tokenStore.consumeRefreshToken("refresh-id", 7L))
                .count();

        assertEquals(1L, successfulConsumers);
    }
}
