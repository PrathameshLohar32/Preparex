package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.request.SubmitRequestDto;
import com.preparex.preparex_backend.dto.response.DailyCompletionResponseDto;
import com.preparex.preparex_backend.dto.response.DailyProblemResponseDto;
import com.preparex.preparex_backend.dto.response.UserStreakResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for daily challenge operations.
 * Handles today's problems, completion, calendar, and streak.
 */
public interface DailyProblemService {

    /**
     * Returns today's 3 daily problems (one per subject).
     * Redis-cached until midnight.
     *
     * @param userId the authenticated user
     * @return list of daily problem DTOs with completion status
     */
    List<DailyProblemResponseDto> getToday(UUID userId);

    /**
     * Completes a daily problem. Creates submission with source=DAILY.
     * Idempotent — duplicate POST returns existing completion.
     *
     * @param userId         the authenticated user
     * @param dailyProblemId the daily problem to complete
     * @param request        submission request with answer
     * @return completion result with scoring
     */
    DailyCompletionResponseDto complete(UUID userId, Integer dailyProblemId, SubmitRequestDto request);

    /**
     * Returns a 90-day calendar map with status per date.
     * Statuses: SOLVED, MISSED, FUTURE.
     *
     * @param userId the authenticated user
     * @return date → status map for last 90 days
     */
    Map<LocalDate, String> getCalendar(UUID userId);

    /**
     * Returns the user's streak information.
     * Redis-cached for 30 minutes.
     *
     * @param userId the authenticated user
     * @return streak DTO with current, longest, lastActiveDate
     */
    UserStreakResponseDto getStreak(UUID userId);
}
