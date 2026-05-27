package com.preparex.preparex_backend.util;

import com.preparex.preparex_backend.config.AppAuthProperties;
import com.preparex.preparex_backend.constant.SecurityConstants;
import com.preparex.preparex_backend.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT access token generation and validation.
 * Tokens are signed with HMAC-SHA256 using the configured secret.
 *
 * Note: Access tokens are NEVER logged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final AppAuthProperties authProperties;

    /**
     * Generates a signed JWT access token embedding userId, email, roles, and sessionId.
     */
    public String generateAccessToken(String userId, String email, List<String> roles, String sessionId) {
        Instant now = Instant.now();
        long expirySeconds = authProperties.getJwt().getAccessTokenExpiryMinutes() * 60L;
        Instant expiry = now.plusSeconds(expirySeconds);

        return Jwts.builder()
                .subject(userId)
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .claim(SecurityConstants.CLAIM_EMAIL, email)
                .claim(SecurityConstants.CLAIM_ROLES, roles)
                .claim(SecurityConstants.CLAIM_SESSION_ID, sessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates and parses a JWT token, returning its claims.
     * Throws InvalidTokenException for any validation failure.
     */
    public Claims validateAndExtractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token has expired");
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("Token is malformed");
        } catch (SecurityException e) {
            throw new InvalidTokenException("Token signature is invalid");
        } catch (JwtException e) {
            throw new InvalidTokenException("Token validation failed");
        }
    }

    /**
     * Generates a secure, random, opaque refresh token (UUID-based).
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    public String extractSessionId(Claims claims) {
        return claims.get(SecurityConstants.CLAIM_SESSION_ID, String.class);
    }

    public String extractUserId(Claims claims) {
        return claims.get(SecurityConstants.CLAIM_USER_ID, String.class);
    }

    public long getAccessTokenExpirySeconds() {
        return authProperties.getJwt().getAccessTokenExpiryMinutes() * 60L;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
