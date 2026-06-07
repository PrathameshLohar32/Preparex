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

    // ── Phase 2: Daily Challenge & Streak ───────────────────────────────

    /** Cached today's daily problems. Key: daily:today */
    public static final String DAILY_TODAY_KEY = "daily:today";

    /** Cached user streak. Key: streak:{userId} */
    public static final String STREAK_CACHE_PREFIX = "streak:";

    /** TTL for cached streak data in minutes */
    public static final long STREAK_CACHE_TTL_MINUTES = 30;

    public static String streakCacheKey(String userId) {
        return STREAK_CACHE_PREFIX + userId;
    }

    // ── Phase 3: Contest Engine ─────────────────────────────────────────

    /** Redis ZSet for contest leaderboard. Key: contest:leaderboard:{contestId} */
    public static final String CONTEST_LEADERBOARD_PREFIX = "contest:leaderboard:";

    /** Distributed lock for contest state transitions. Key: contest:state:{contestId} */
    public static final String CONTEST_STATE_LOCK_PREFIX = "contest:state:";

    /** Distributed lock for contest registration. Key: contest:register:{contestId}:{userId} */
    public static final String CONTEST_REGISTER_LOCK_PREFIX = "contest:register:";

    public static String contestLeaderboardKey(String contestId) {
        return CONTEST_LEADERBOARD_PREFIX + contestId;
    }

    // ── Phase 4: Sprint Mode ────────────────────────────────────────────

    /** Sprint session state. Key: sprint:session:{sessionId} */
    public static final String SPRINT_SESSION_PREFIX = "sprint:session:";

    /** Lock to prevent duplicate sprint sessions per user. Key: sprint:user:{userId} */
    public static final String SPRINT_USER_LOCK_PREFIX = "sprint:user:";

    /** Weekly sprint leaderboard ZSet. Key: sprint:leaderboard:weekly:{YYYY-WW} */
    public static final String SPRINT_LEADERBOARD_WEEKLY_PREFIX = "sprint:leaderboard:weekly:";

    /** Monthly sprint leaderboard ZSet. Key: sprint:leaderboard:monthly:{YYYY-MM} */
    public static final String SPRINT_LEADERBOARD_MONTHLY_PREFIX = "sprint:leaderboard:monthly:";

    public static String sprintSessionKey(String sessionId) {
        return SPRINT_SESSION_PREFIX + sessionId;
    }

    public static String sprintUserLockKey(String userId) {
        return SPRINT_USER_LOCK_PREFIX + userId;
    }

    public static String sprintWeeklyLeaderboardKey(String weekKey) {
        return SPRINT_LEADERBOARD_WEEKLY_PREFIX + weekKey;
    }

    public static String sprintMonthlyLeaderboardKey(String monthKey) {
        return SPRINT_LEADERBOARD_MONTHLY_PREFIX + monthKey;
    }

    // ── Phase 5: User Profile & Analytics ───────────────────────────────

    /** Full profile cache. Key: profile:full:{userId}  TTL 15min */
    public static final String PROFILE_FULL_PREFIX = "profile:full:";
    public static final long PROFILE_FULL_TTL_MINUTES = 15;

    /** Heatmap cache. Key: profile:heatmap:{userId}  TTL 1hr */
    public static final String PROFILE_HEATMAP_PREFIX = "profile:heatmap:";
    public static final long PROFILE_HEATMAP_MINUTES = 60;

    /** Contest history cache. Key: profile:contest:{userId}  TTL 30min */
    public static final String PROFILE_CONTEST_PREFIX = "profile:contest:";
    public static final long PROFILE_CONTEST_TTL_MINUTES = 30;

    /** Badges cache. Key: profile:badges:{userId}  TTL 30min */
    public static final String PROFILE_BADGES_PREFIX = "profile:badges:";
    public static final long PROFILE_BADGES_TTL_MINUTES = 30;

    /** Solved stats cache. Key: profile:stats:{userId}  TTL 1hr */
    public static final String PROFILE_STATS_PREFIX = "profile:stats:";
    public static final long PROFILE_STATS_TTL_MINUTES = 60;

    /** Subject stats cache. Key: profile:subject:{userId}  TTL 1hr */
    public static final String PROFILE_SUBJECT_PREFIX = "profile:subject:";
    public static final long PROFILE_SUBJECT_TTL_MINUTES = 60;

    /** Sprint stats cache. Key: profile:sprint:{userId}  TTL 30min */
    public static final String PROFILE_SPRINT_PREFIX = "profile:sprint:";
    public static final long PROFILE_SPRINT_TTL_MINUTES = 30;

    public static String profileFullKey(String userId) {
        return PROFILE_FULL_PREFIX + userId;
    }

    public static String profileHeatmapKey(String userId) {
        return PROFILE_HEATMAP_PREFIX + userId;
    }

    public static String profileContestKey(String userId) {
        return PROFILE_CONTEST_PREFIX + userId;
    }

    public static String profileBadgesKey(String userId) {
        return PROFILE_BADGES_PREFIX + userId;
    }

    public static String profileStatsKey(String userId) {
        return PROFILE_STATS_PREFIX + userId;
    }

    public static String profileSubjectKey(String userId) {
        return PROFILE_SUBJECT_PREFIX + userId;
    }

    public static String profileSprintKey(String userId) {
        return PROFILE_SPRINT_PREFIX + userId;
    }
}
