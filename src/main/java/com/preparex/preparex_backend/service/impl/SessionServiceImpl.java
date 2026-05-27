package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.config.AppAuthProperties;
import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.enums.LogoutReason;
import com.preparex.preparex_backend.redis.model.ActiveSessionData;
import com.preparex.preparex_backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis-backed session management service.
 *
 * Session storage:
 *   - session:{sessionId}          → ActiveSessionData (with TTL)
 *   - user:{userId}:sessions       → Set<sessionId> (tracks all session IDs for a user)
 *
 * Max session enforcement:
 *   - When a new session is created and the user already has maxActiveSessions,
 *     the oldest session (by createdAt) is evicted before creating the new one.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AppAuthProperties authProperties;

    @Override
    public ActiveSessionData createSession(String userId, String refreshTokenHash,
                                            String deviceInfo, String ipAddress, String userAgent) {
        enforceSessionLimit(userId);

        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        ActiveSessionData session = ActiveSessionData.builder()
                .sessionId(sessionId)
                .userId(userId)
                .refreshTokenHash(refreshTokenHash)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(now)
                .lastAccessedAt(now)
                .build();

        long ttlDays = authProperties.getSession().getTtlDays();
        String sessionKey = RedisKeyConstants.sessionKey(sessionId);
        String userSessionsKey = RedisKeyConstants.userSessionsKey(userId);

        redisTemplate.opsForValue().set(sessionKey, session, ttlDays, TimeUnit.DAYS);
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        redisTemplate.expire(userSessionsKey, ttlDays, TimeUnit.DAYS);

        log.info("Session created sessionId={} userId={}", sessionId, userId);
        return session;
    }

    @Override
    public ActiveSessionData getSession(String sessionId) {
        Object data = redisTemplate.opsForValue().get(RedisKeyConstants.sessionKey(sessionId));
        if (data instanceof ActiveSessionData session) {
            return session;
        }
        return null;
    }

    @Override
    public boolean isSessionActive(String sessionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyConstants.sessionKey(sessionId)));
    }

    @Override
    public void invalidateSession(String sessionId, String userId) {
        redisTemplate.delete(RedisKeyConstants.sessionKey(sessionId));
        redisTemplate.opsForSet().remove(RedisKeyConstants.userSessionsKey(userId), sessionId);
        log.info("Session invalidated sessionId={} userId={}", sessionId, userId);
    }

    @Override
    public void invalidateAllSessions(String userId) {
        Set<Object> sessionIds = redisTemplate.opsForSet().members(RedisKeyConstants.userSessionsKey(userId));
        if (sessionIds != null) {
            sessionIds.forEach(id -> redisTemplate.delete(RedisKeyConstants.sessionKey(id.toString())));
        }
        redisTemplate.delete(RedisKeyConstants.userSessionsKey(userId));
        log.info("All sessions invalidated for userId={}", userId);
    }

    @Override
    public List<ActiveSessionData> getActiveSessions(String userId) {
        Set<Object> sessionIds = redisTemplate.opsForSet().members(RedisKeyConstants.userSessionsKey(userId));
        if (sessionIds == null || sessionIds.isEmpty()) {
            return List.of();
        }

        return sessionIds.stream()
                .map(id -> getSession(id.toString()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ActiveSessionData::getCreatedAt))
                .collect(Collectors.toList());
    }

    @Override
    public void touchSession(String sessionId) {
        ActiveSessionData session = getSession(sessionId);
        if (session != null) {
            session.setLastAccessedAt(Instant.now());
            long ttlDays = authProperties.getSession().getTtlDays();
            redisTemplate.opsForValue().set(
                    RedisKeyConstants.sessionKey(sessionId), session, ttlDays, TimeUnit.DAYS);
        }
    }

    /**
     * Enforces the max active sessions limit.
     * If the user already has maxActiveSessions, the oldest session is evicted silently.
     */
    private void enforceSessionLimit(String userId) {
        int maxSessions = authProperties.getSession().getMaxActiveSessions();
        List<ActiveSessionData> activeSessions = getActiveSessions(userId);

        while (activeSessions.size() >= maxSessions) {
            ActiveSessionData oldest = activeSessions.get(0);
            log.info("Session limit reached. Evicting oldest session sessionId={} userId={}",
                    oldest.getSessionId(), userId);
            invalidateSession(oldest.getSessionId(), userId);
            activeSessions.remove(0);
        }
    }
}
