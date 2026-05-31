package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.request.CreateProblemRequestDto;
import com.preparex.preparex_backend.dto.request.ProblemFilterRequestDto;
import com.preparex.preparex_backend.dto.request.UpdateProblemRequestDto;
import com.preparex.preparex_backend.dto.response.PassageResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemDetailResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemListItemResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for problem-related operations.
 * Provides both user-facing read operations and admin CRUD.
 */
public interface ProblemService {

    /**
     * Returns a paginated, filtered list of problems.
     * answer_key is never included in list responses.
     *
     * @param filter filter and pagination parameters
     * @param userId current user ID (for future isSolved computation)
     * @return paginated problem list items
     */
    Page<ProblemListItemResponseDto> getProblems(ProblemFilterRequestDto filter, UUID userId);

    /**
     * Returns full problem detail by ID (without answer_key).
     * Result is cached via the CachedProblemService decorator.
     *
     * @param id     problem UUID
     * @param userId current user ID
     * @return problem detail
     */
    ProblemDetailResponseDto getProblemById(UUID id, UUID userId);

    /**
     * Returns parent problem + all child questions for a passage-type problem.
     *
     * @param parentId parent problem UUID
     * @return passage with parent and children
     */
    PassageResponseDto getPassage(UUID parentId);

    /**
     * Returns the hints array for a problem.
     * Client is responsible for revealing hints one at a time.
     *
     * @param problemId problem UUID
     * @return list of hint strings
     */
    List<String> getHints(UUID problemId);

    /**
     * Returns the solution for a problem.
     * In Phase 2, this will require the user to have at least one submission.
     *
     * @param problemId problem UUID
     * @param userId    current user ID
     * @return problem detail including solution text
     */
    ProblemDetailResponseDto getSolution(UUID problemId, UUID userId);

    // ── Admin Operations ────────────────────────────────────────────────

    /**
     * Creates a new problem (admin use).
     *
     * @param dto create request with all problem fields
     * @return created problem detail
     */
    ProblemDetailResponseDto createProblem(CreateProblemRequestDto dto);

    /**
     * Updates an existing problem (admin use).
     * Evicts the Redis cache for the updated problem.
     *
     * @param id  problem UUID
     * @param dto update request with changed fields
     * @return updated problem detail
     */
    ProblemDetailResponseDto updateProblem(UUID id, UpdateProblemRequestDto dto);

    /**
     * Soft-deletes a problem by setting is_active = false.
     * Evicts the Redis cache for the deleted problem.
     *
     * @param id problem UUID
     */
    void deleteProblem(UUID id);
}
