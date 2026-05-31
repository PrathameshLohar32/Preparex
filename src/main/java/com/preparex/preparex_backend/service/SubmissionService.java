package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.request.SubmissionFilterRequestDto;
import com.preparex.preparex_backend.dto.request.SubmitRequestDto;
import com.preparex.preparex_backend.dto.response.SubmissionHistoryResponseDto;
import com.preparex.preparex_backend.dto.response.SubmissionResponseDto;
import com.preparex.preparex_backend.dto.response.SubjectStatsResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Service interface for submission operations.
 * Handles scoring, history retrieval, and subject statistics.
 */
public interface SubmissionService {

    /**
     * Scores and persists a submission. Publishes SubmissionSavedEvent.
     *
     * @param userId  the authenticated user
     * @param request submission details including answer
     * @return scoring result (never includes answer_key)
     */
    SubmissionResponseDto submit(UUID userId, SubmitRequestDto request);

    /**
     * Returns paginated submission history with optional filters.
     *
     * @param userId the authenticated user
     * @param filter filter and pagination parameters
     * @return paginated submission history
     */
    Page<SubmissionHistoryResponseDto> getHistory(UUID userId, SubmissionFilterRequestDto filter);

    /**
     * Returns subject-wise, difficulty-wise submission statistics.
     *
     * @param userId the authenticated user
     * @return aggregated stats
     */
    SubjectStatsResponseDto getStats(UUID userId);
}
