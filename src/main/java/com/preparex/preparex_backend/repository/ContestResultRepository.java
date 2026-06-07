package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.ContestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContestResultRepository extends JpaRepository<ContestResult, UUID> {

    Optional<ContestResult> findByContestIdAndUserId(UUID contestId, UUID userId);

    Page<ContestResult> findByContestIdOrderByRankAsc(UUID contestId, Pageable pageable);

    boolean existsByContestId(UUID contestId);

    /**
     * Paginated contest history for a user, most recent first.
     * Used in profile contest-history and contest-graph endpoints.
     */
    Page<ContestResult> findByUserIdOrderByFinalizedAtDesc(UUID userId, Pageable pageable);

    /**
     * All contest results for a user (unpaginated).
     * Used for contest-graph line chart data.
     */
    List<ContestResult> findByUserIdOrderByFinalizedAtAsc(UUID userId);
}

