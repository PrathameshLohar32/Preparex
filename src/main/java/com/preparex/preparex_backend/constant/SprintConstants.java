package com.preparex.preparex_backend.constant;

/**
 * Sprint mode constants for point scoring, session limits, and configuration.
 * Centralizes all magic numbers used in sprint logic.
 */
public final class SprintConstants {

    private SprintConstants() {}

    // ── Point Values ────────────────────────────────────────────────────

    /** Points awarded for correctly answering an EASY question */
    public static final int EASY_CORRECT = 5;

    /** Points awarded for correctly answering a MEDIUM question */
    public static final int MEDIUM_CORRECT = 10;

    /** Points awarded for correctly answering a HARD question */
    public static final int HARD_CORRECT = 15;

    /** Points for a wrong answer */
    public static final int WRONG = 0;

    /** Points for a skipped question */
    public static final int SKIPPED = 0;

    // ── Bonus Points ────────────────────────────────────────────────────

    /** Extra points if answered on first attempt (not skipped and returned) */
    public static final int FIRST_ATTEMPT_BONUS = 2;

    /** Time bonus: answered in under 30 seconds */
    public static final int TIME_BONUS_FAST = 2;

    /** Time bonus: answered in under 60 seconds */
    public static final int TIME_BONUS_MODERATE = 1;

    /** Threshold (seconds) for fast time bonus */
    public static final int TIME_BONUS_FAST_THRESHOLD_SECS = 30;

    /** Threshold (seconds) for moderate time bonus */
    public static final int TIME_BONUS_MODERATE_THRESHOLD_SECS = 60;

    // ── Session Limits ──────────────────────────────────────────────────

    /** Maximum number of skips allowed per sprint session */
    public static final int MAX_SKIPS = 5;

    /** Sprint session duration in minutes */
    public static final int SESSION_DURATION_MINS = 30;

    /** Number of questions pre-generated for a sprint session queue */
    public static final int QUESTION_QUEUE_SIZE = 60;

    /** Redis TTL for sprint session state in minutes (5min buffer beyond session duration) */
    public static final int SESSION_REDIS_TTL_MINS = 35;

    // ── Leaderboard TTLs ────────────────────────────────────────────────

    /** Weekly leaderboard Redis key TTL in days */
    public static final int WEEKLY_LEADERBOARD_TTL_DAYS = 8;

    /** Monthly leaderboard Redis key TTL in days */
    public static final int MONTHLY_LEADERBOARD_TTL_DAYS = 35;
}
