package com.preparex.preparex_backend.constant;

/**
 * Redis key prefixes and patterns used throughout the auth service.
 * Always use these constants instead of hardcoded strings.
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {}

    /** Active session data. Key: session:{sessionId} */
    public static final String SESSION_PREFIX = "session:";

    /** Set of active session IDs for a user. Key: user:{userId}:sessions */
    public static final String USER_SESSIONS_PREFIX = "user:";
    public static final String USER_SESSIONS_SUFFIX = ":sessions";

    /** Temporary registration data pending OTP verification. Key: temp:reg:{email|phone} */
    public static final String TEMP_REGISTRATION_PREFIX = "temp:reg:";

    /** OTP data for login/verification. Key: otp:{email|phone} */
    public static final String OTP_PREFIX = "otp:";

    /** Rate limiting counters. Key: rate:{action}:{identifier} */
    public static final String RATE_LIMIT_PREFIX = "rate:";

    /** Resend cooldown tracker. Key: otp:resend:{email|phone} */
    public static final String OTP_RESEND_PREFIX = "otp:resend:";

    public static String sessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    public static String userSessionsKey(String userId) {
        return USER_SESSIONS_PREFIX + userId + USER_SESSIONS_SUFFIX;
    }

    public static String tempRegistrationKey(String identifier) {
        return TEMP_REGISTRATION_PREFIX + identifier;
    }

    public static String otpKey(String identifier) {
        return OTP_PREFIX + identifier;
    }

    public static String otpResendKey(String identifier) {
        return OTP_RESEND_PREFIX + identifier;
    }

    public static String rateLimitKey(String action, String identifier) {
        return RATE_LIMIT_PREFIX + action + ":" + identifier;
    }

    // ── Phase 1: Problem Cache ──────────────────────────────────────────

    /** Cached problem detail. Key: problem:{problemId} */
    public static final String PROBLEM_CACHE_PREFIX = "problem:";

    /** TTL for cached problem detail in hours */
    public static final long PROBLEM_CACHE_TTL_HOURS = 1;

    public static String problemCacheKey(String problemId) {
        return PROBLEM_CACHE_PREFIX + problemId;
    }
}
