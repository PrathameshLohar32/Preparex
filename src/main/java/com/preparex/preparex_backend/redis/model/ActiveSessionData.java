package com.preparex.preparex_backend.redis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Stored in Redis under key: session:{sessionId}
 * TTL: configured via app.auth.session.ttl-days
 *
 * Note: Only the refresh token hash is stored, never the raw token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessionData implements Serializable {

    private String sessionId;
    private String userId;
    private String refreshTokenHash;
    private String deviceInfo;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
    private Instant lastAccessedAt;
}
