package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Full user profile response — includes user info, profile data,
 * solved stats, streak, sprint stats, and badges.
 * Cached in Redis for 15 minutes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {

    // ── User Info ───────────────────────────────────────────────────────
    private UUID userId;
    private String name;
    private String username;
    private String email;
    private String phone;

    // ── Profile Data ────────────────────────────────────────────────────
    private String bio;
    private String gender;
    private String location;
    private LocalDate dateOfBirth;
    private String twitterUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String instagramUrl;
    private String theme;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private String dailyReminderTime;

    // ── Solved Stats ────────────────────────────────────────────────────
    private Integer totalSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;

    // ── Streak ──────────────────────────────────────────────────────────
    private Integer currentStreak;
    private Integer longestStreak;

    // ── Sprint Stats ────────────────────────────────────────────────────
    private Integer totalSprints;
    private Integer totalSprintPoints;
    private Integer bestWeeklyRank;

    // ── Badges Summary ──────────────────────────────────────────────────
    private Integer badgesEarned;
    private List<BadgeDto> recentBadges;
}
