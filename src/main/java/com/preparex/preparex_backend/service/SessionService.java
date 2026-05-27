package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.redis.model.ActiveSessionData;

import java.util.List;

public interface SessionService {

    /**
     * Creates a new session in Redis and enforces the max active sessions limit.
     * If the limit is exceeded, the oldest session is evicted.
     */
    ActiveSessionData createSession(String userId, String refreshTokenHash,
                                    String deviceInfo, String ipAddress, String userAgent);

    /**
     * Retrieves active session data from Redis. Returns null if not found.
     */
    ActiveSessionData getSession(String sessionId);

    /**
     * Returns true if the session exists in Redis.
     */
    boolean isSessionActive(String sessionId);

    /**
     * Removes a single session from Redis and user's session set.
     */
    void invalidateSession(String sessionId, String userId);

    /**
     * Removes all sessions for a user from Redis.
     */
    void invalidateAllSessions(String userId);

    /**
     * Returns all active session data objects for a user.
     */
    List<ActiveSessionData> getActiveSessions(String userId);

    /**
     * Updates the lastAccessedAt timestamp for a session (refresh rotation).
     */
    void touchSession(String sessionId);
}
