package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.enums.BadgeType;

import java.util.UUID;

/**
 * Service interface for badge management.
 * Handles idempotent award logic and threshold-based auto-awarding.
 */
public interface BadgeService {

    /**
     * Awards a badge to a user. Idempotent — no-op if already awarded.
     * Persists to user_badges and publishes BadgeAwardedEvent to Kafka.
     *
     * @param userId  the user to award the badge to
     * @param type    the badge type
     * @param context optional context (e.g. "7-day streak on 2026-06-06")
     */
    void award(UUID userId, BadgeType type, String context);

    /**
     * Checks streak milestones (7, 30, 100) and awards badges if thresholds are met.
     */
    void checkAndAwardStreakBadges(UUID userId, int currentStreak);

    /**
     * Checks solved count milestones (50, 100, 500) and awards badges if thresholds are met.
     */
    void checkAndAwardSolvedBadges(UUID userId, int totalSolved);

    /**
     * Checks contest performance and awards appropriate badges.
     */
    void checkAndAwardContestBadges(UUID userId, UUID contestId, int rank, double percentile);
}
