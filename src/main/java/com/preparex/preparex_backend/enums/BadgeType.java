package com.preparex.preparex_backend.enums;

/**
 * Badge types that can be earned by users.
 * Used in user_badges table and BadgeService for automated awarding.
 */
public enum BadgeType {
    // ── Streak Badges ───────────────────────────────────────────────────
    STREAK_7,
    STREAK_30,
    STREAK_100,

    // ── Solved Count Badges ─────────────────────────────────────────────
    SOLVED_50,
    SOLVED_100,
    SOLVED_500,

    // ── Contest Badges ──────────────────────────────────────────────────
    CONTEST_PARTICIPANT,
    CONTEST_TOP_10_PERCENT,
    CONTEST_WINNER,
    PERFECT_CONTEST_SCORE,

    // ── Sprint Badges ───────────────────────────────────────────────────
    SPRINT_CHAMPION_WEEKLY,
    SPRINT_CHAMPION_MONTHLY,

    // ── Subject Mastery Badges ──────────────────────────────────────────
    PHYSICS_MASTER,
    CHEMISTRY_MASTER,
    MATHS_MASTER
}
