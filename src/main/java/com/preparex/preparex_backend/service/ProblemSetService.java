package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.request.AddProblemToSetRequestDto;
import com.preparex.preparex_backend.dto.request.CreateProblemSetRequestDto;
import com.preparex.preparex_backend.dto.response.ProblemSetProgressResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemSetResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for problem set operations.
 */
public interface ProblemSetService {

    /**
     * Returns all active problem sets with completion metrics.
     * Premium sets show locked=true for free users.
     *
     * @param userId        current user ID
     * @param isPremiumUser whether the user has premium access
     * @return list of problem set DTOs
     */
    List<ProblemSetResponseDto> getAllSets(UUID userId, boolean isPremiumUser);

    /**
     * Returns a specific problem set by slug with problems and completion data.
     *
     * @param slug   problem set slug
     * @param userId current user ID
     * @return problem set detail
     */
    ProblemSetResponseDto getSetBySlug(String slug, UUID userId);

    /**
     * Returns the user's progress for a specific problem set.
     *
     * @param slug   problem set slug
     * @param userId current user ID
     * @return progress with total, completed, percentage
     */
    ProblemSetProgressResponseDto getProgress(String slug, UUID userId);

    // ── Admin Operations ────────────────────────────────────────────────

    /**
     * Creates a new curated problem set (admin use).
     *
     * @param dto create request
     * @return created problem set DTO
     */
    ProblemSetResponseDto createProblemSet(CreateProblemSetRequestDto dto);

    /**
     * Adds a problem to an existing problem set (admin use).
     *
     * @param setId problem set UUID
     * @param dto   add request with problem ID and position
     */
    void addProblemToSet(UUID setId, AddProblemToSetRequestDto dto);
}
