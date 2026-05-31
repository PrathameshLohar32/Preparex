package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.DailyCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for DailyCompletion entity.
 * Supports idempotent completion checks and calendar view queries.
 */
@Repository
public interface DailyCompletionRepository extends JpaRepository<DailyCompletion, Long> {

    Optional<DailyCompletion> findByUserIdAndDailyProblemId(UUID userId, Integer dailyProblemId);

    boolean existsByUserIdAndDailyProblemId(UUID userId, Integer dailyProblemId);

    /**
     * Finds all daily completions for a user within a date range.
     * Used for the 90-day calendar view.
     */
    List<DailyCompletion> findByUserIdAndCompletedDateBetween(
            UUID userId, LocalDate startDate, LocalDate endDate);

    /**
     * Counts how many daily problems a user completed on a given date.
     */
    long countByUserIdAndCompletedDate(UUID userId, LocalDate completedDate);
}
