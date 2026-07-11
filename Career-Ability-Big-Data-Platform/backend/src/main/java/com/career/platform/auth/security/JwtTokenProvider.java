package com.career.platform.auth.security;

import com.career.platform.common.error.BusinessException;
import com.career.platform.common.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public static final String ACCESS_TOKEN = "access";
    public static final String REFRESH_TOKEN = "refresh";

    private final JwtProperties properties;
    private final Clock clock;
    private SecretKey signingKey;

    @Autowired
    public JwtTokenProvider(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    JwtTokenProvider(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @PostConstruct
    void initializeKey() {
        String secret = properties.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        }
        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public IssuedToken createAccessToken(Long userId, String username) {
        return createToken(userId, username, ACCESS_TOKEN, properties.getAccessTokenTtl());
    }

    public IssuedToken createRefreshToken(Long userId, String username) {
        return createToken(userId, username, REFRESH_TOKEN, properties.getRefreshTokenTtl());
    }

    public TokenClaims parseAccessToken(String token) {
        return parse(token, ACCESS_TOKEN);
    }

    public TokenClaims parseRefreshToken(String token) {
        return parse(token, REFRESH_TOKEN);
    }

    public Duration getAccessTokenTtl() {
        return properties.getAccessTokenTtl();
    }

    public Duration remainingValidity(TokenClaims claims) {
        Duration remaining = Duration.between(clock.instant(), claims.getExpiresAt());
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    private IssuedToken createToken(
            Long userId,
            String username,
            String tokenType,
            Duration ttl) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(ttl);
        String tokenId = UUID.randomUUID().toString();
        String value = Jwts.builder()
                .setSubject(username)
                .setId(tokenId)
                .claim("uid", userId)
                .claim("type", tokenType)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new IssuedToken(value, tokenId, expiresAt);
    }

    private TokenClaims parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .setClock(() -> Date.from(clock.instant()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String actualType = claims.get("type", String.class);
            Number userId = claims.get("uid", Number.class);
            if (!expectedType.equals(actualType) || userId == null || claims.getId() == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token type or claims");
            }
            return new TokenClaims(
                    userId.longValue(),
                    claims.getSubject(),
                    claims.getId(),
                    actualType,
                    claims.getExpiration().toInstant());
        } catch (BusinessException exception) {
            throw exception;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid or expired token");
        }
    }
}
