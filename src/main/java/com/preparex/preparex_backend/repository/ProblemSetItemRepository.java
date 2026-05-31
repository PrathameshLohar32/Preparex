package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.ProblemSetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ProblemSetItem join entity.
 * Provides ordered problem listing within a set and count operations.
 */
@Repository
public interface ProblemSetItemRepository extends JpaRepository<ProblemSetItem, Integer> {

    List<ProblemSetItem> findByProblemSetIdOrderByPositionAsc(UUID setId);

    long countByProblemSetId(UUID setId);

    boolean existsByProblemSetIdAndProblemId(UUID setId, UUID problemId);

    /**
     * Returns the problem IDs belonging to a given set, ordered by position.
     */
    @Query("SELECT psi.problem.id FROM ProblemSetItem psi WHERE psi.problemSet.id = :setId ORDER BY psi.position ASC")
    List<UUID> findProblemIdsBySetId(@Param("setId") UUID setId);
}
