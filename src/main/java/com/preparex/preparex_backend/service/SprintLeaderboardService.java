package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.response.SprintLeaderboardEntryDto;
import com.preparex.preparex_backend.dto.response.SprintRankDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for sprint leaderboard operations.
 * Manages weekly and monthly Redis ZSet-based leaderboards.
 */
public interface SprintLeaderboardService {

    /**
     * Adds (increments) points to both weekly and monthly leaderboards for a user.
     * Uses ZADD with INCR semantics to accumulate points across multiple sprints.
     *
     * @param userId the user whose points to update
     * @param points the points to add
     */
    void addPoints(UUID userId, int points);

    /**
     * Returns the top entries from the current week's leaderboard.
     *
     * @param limit maximum number of entries to return
     * @return ranked list of leaderboard entries
     */
    List<SprintLeaderboardEntryDto> getWeeklyTop(int limit);

    /**
     * Returns the top entries from the current month's leaderboard.
     *
     * @param limit maximum number of entries to return
     * @return ranked list of leaderboard entries
     */
    List<SprintLeaderboardEntryDto> getMonthlyTop(int limit);

    /**
     * Returns a user's rank and points in the current week's leaderboard.
     *
     * @param userId the user to look up
     * @return rank, points, and total participants
     */
    SprintRankDto getUserWeeklyRank(UUID userId);
}
