package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.DailyProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for DailyProblem entity.
 * Supports lookup by scheduled date and problem selection for scheduler.
 */
@Repository
public interface DailyProblemRepository extends JpaRepository<DailyProblem, Integer> {

    List<DailyProblem> findByScheduledDateAndIsActiveTrue(LocalDate scheduledDate);

    boolean existsByScheduledDate(LocalDate scheduledDate);

    /**
     * Finds problem IDs already used as daily problems, to avoid repetition.
     */
    @Query("SELECT dp.problem.id FROM DailyProblem dp WHERE dp.subject.id = :subjectId")
    List<UUID> findUsedProblemIdsBySubjectId(@Param("subjectId") Integer subjectId);
}
