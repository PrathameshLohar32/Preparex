package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.request.SprintAnswerRequestDto;
import com.preparex.preparex_backend.dto.request.SprintStartRequestDto;
import com.preparex.preparex_backend.dto.response.*;

import java.util.UUID;

/**
 * Service interface for sprint mode operations.
 * Manages the complete lifecycle of a 30-minute timed blitz session.
 */
public interface SprintService {

    /**
     * Starts a new sprint session for the user.
     * Enforces one-active-session-per-user via RLock.
     * Generates a queue of 60 questions, saves state to Redis, and persists the session entity.
     *
     * @param userId the user starting the sprint
     * @param request optional filters (subject, difficulty, exam)
     * @return session ID, first question, and session metadata
     */
    SprintStartResponseDto startSprint(UUID userId, SprintStartRequestDto request);

    /**
     * Scores the user's answer to the current question.
     * Calculates points with time bonus and first-attempt bonus.
     * Auto-ends the session if 30min has elapsed.
     *
     * @param userId    the authenticated user
     * @param sessionId the active sprint session
     * @param request   the answer data
     * @return scoring result, next question, time remaining, and session stats
     */
    SprintAnswerResponseDto answerQuestion(UUID userId, UUID sessionId, SprintAnswerRequestDto request);

    /**
     * Skips the current question (max 5 skips per session).
     * The skipped question is recycled to the back of the queue.
     *
     * @param userId    the authenticated user
     * @param sessionId the active sprint session
     * @return the next question and updated session stats
     */
    SprintAnswerResponseDto skipQuestion(UUID userId, UUID sessionId);

    /**
     * Ends the sprint session, either by user choice or auto-expiry.
     * Calculates final points, updates leaderboard, and deletes Redis session state.
     *
     * @param userId    the authenticated user
     * @param sessionId the sprint session to end
     * @return full sprint summary with scoring breakdown
     */
    SprintSummaryDto endSprint(UUID userId, UUID sessionId);

    /**
     * Returns the current status of an active sprint session.
     * Includes time remaining, current question, and running stats.
     *
     * @param userId    the authenticated user
     * @param sessionId the sprint session to check
     * @return current session state
     */
    SprintStatusResponseDto getStatus(UUID userId, UUID sessionId);
}
