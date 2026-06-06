package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.SprintAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for SprintAnswer entity.
 * Provides queries for session answer history and duplicate prevention.
 */
@Repository
public interface SprintAnswerRepository extends JpaRepository<SprintAnswer, UUID> {

    /**
     * Loads all answers for a sprint session.
     * Used when building the end-of-sprint summary.
     */
    List<SprintAnswer> findBySessionIdOrderByAnsweredAtAsc(UUID sessionId);

    /**
     * Checks if a problem was already answered in a session.
     * Prevents duplicate answers for the same question.
     */
    boolean existsBySessionIdAndProblemId(UUID sessionId, UUID problemId);
}
