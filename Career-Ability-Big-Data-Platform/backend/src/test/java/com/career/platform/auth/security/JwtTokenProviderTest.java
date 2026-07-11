package com.career.platform.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.career.platform.common.error.BusinessException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-07-10T00:00:00Z");

    @Test
    void createsAndParsesAccessTokenClaims() {
        JwtTokenProvider provider = providerAt(ISSUED_AT);

        TokenClaims claims = provider.parseAccessToken(
                provider.createAccessToken(42L, "student01").getValue());

        assertEquals(42L, claims.getUserId());
        assertEquals("student01", claims.getUsername());
        assertEquals(JwtTokenProvider.ACCESS_TOKEN, claims.getTokenType());
    }

    @Test
    void rejectsTokenTypeMixingAndTampering() {
        JwtTokenProvider provider = providerAt(ISSUED_AT);
        String refreshToken = provider.createRefreshToken(42L, "student01").getValue();
        String accessToken = provider.createAccessToken(42L, "student01").getValue();
        int changedIndex = accessToken.length() - 2;
        char replacement = accessToken.charAt(changedIndex) == 'a' ? 'b' : 'a';
        String tampered = accessToken.substring(0, changedIndex)
                + replacement
                + accessToken.substring(changedIndex + 1);

        assertThrows(BusinessException.class, () -> provider.parseAccessToken(refreshToken));
        assertThrows(BusinessException.class, () -> provider.parseAccessToken(tampered));
    }

    @Test
    void rejectsExpiredToken() {
        String token = providerAt(ISSUED_AT).createAccessToken(42L, "student01").getValue();
        JwtTokenProvider futureProvider = providerAt(ISSUED_AT.plus(Duration.ofHours(3)));

        assertThrows(BusinessException.class, () -> futureProvider.parseAccessToken(token));
    }

    private JwtTokenProvider providerAt(Instant instant) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-only-secret-key-that-is-longer-than-thirty-two-bytes");
        properties.setAccessTokenTtl(Duration.ofHours(2));
        properties.setRefreshTokenTtl(Duration.ofDays(7));
        JwtTokenProvider provider = new JwtTokenProvider(
                properties,
                Clock.fixed(instant, ZoneOffset.UTC));
        provider.initializeKey();
        return provider;
    }
}
