package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.request.UpdateProfileRequestDto;
import com.preparex.preparex_backend.dto.response.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for user profile and analytics operations.
 * All read operations use Redis caching with event-driven invalidation.
 */
public interface ProfileService {

    /**
     * Returns the full profile for the authenticated user.
     * Includes user info, profile data, solved stats, streak, sprint stats, badges.
     * Redis cached for 15 minutes.
     */
    UserProfileResponseDto getFullProfile(UUID userId);

    /**
     * Returns a public profile for viewing other users.
     * Excludes email and phone.
     */
    PublicProfileResponseDto getPublicProfile(UUID userId);

    /**
     * Updates the user's profile data (bio, social links, preferences).
     * Evicts the profile cache on update.
     */
    UserProfileResponseDto updateProfile(UUID userId, UpdateProfileRequestDto request);

    /**
     * Returns the 365-day activity heatmap.
     * Merges daily completions + practice submissions grouped by date.
     * Redis cached for 1 hour.
     */
    List<HeatmapEntryDto> getHeatmap(UUID userId);

    /**
     * Returns paginated contest history for the user.
     * Redis cached for 30 minutes.
     */
    Page<ContestHistoryDto> getContestHistory(UUID userId, int page, int size);

    /**
     * Returns contest score/rank progression for line chart.
     * Sorted by date ascending.
     */
    List<ContestGraphEntryDto> getContestGraph(UUID userId);

    /**
     * Returns per-subject accuracy for radar chart.
     * Redis cached for 1 hour.
     */
    List<SubjectGraphEntryDto> getSubjectGraph(UUID userId);

    /**
     * Returns earned and locked badges.
     * Redis cached for 30 minutes.
     */
    BadgesResponseDto getBadges(UUID userId);

    /**
     * Admin: Deletes and recomputes all stats from raw submissions.
     * Used as recovery mechanism if Kafka consumers miss events.
     */
    void recalculateStats(UUID userId);
}
