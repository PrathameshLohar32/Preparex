package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.Submission;
import com.preparex.preparex_backend.enums.SubmissionSource;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Submission entity.
 * Provides history, stats, and existence checks for submissions.
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    /**
     * Dynamic filter query for submission history.
     * All filter parameters are optional — null values are ignored.
     */
    @Query("""
            SELECT s FROM Submission s
            JOIN FETCH s.problem p
            WHERE s.user.id = :userId
              AND (:problemId IS NULL OR s.problem.id = :problemId)
              AND (:status IS NULL OR s.status = :status)
              AND (:source IS NULL OR s.source = :source)
            ORDER BY s.submittedAt DESC
            """)
    Page<Submission> findByFilters(
            @Param("userId") UUID userId,
            @Param("problemId") UUID problemId,
            @Param("status") SubmissionStatus status,
            @Param("source") SubmissionSource source,
            Pageable pageable
    );

    /**
     * Checks if a user has at least one submission for a given problem.
     * Used to gate solution access.
     */
    boolean existsByUserIdAndProblemId(UUID userId, UUID problemId);

    /**
     * Returns all problem IDs that a user has solved (status=CORRECT).
     * Used for isSolved flag in problem listings.
     */
    @Query("SELECT DISTINCT s.problem.id FROM Submission s WHERE s.user.id = :userId AND s.status = 'CORRECT'")
    List<UUID> findSolvedProblemIdsByUserId(@Param("userId") UUID userId);

    /**
     * Counts submissions grouped by subject, difficulty, and status for stats.
     */
    @Query("""
            SELECT p.subject.name, p.difficulty, s.status, COUNT(s)
            FROM Submission s
            JOIN s.problem p
            WHERE s.user.id = :userId
            GROUP BY p.subject.name, p.difficulty, s.status
            """)
    List<Object[]> findSubjectStats(@Param("userId") UUID userId);

    long countByUserIdAndProblemId(UUID userId, UUID problemId);
}
