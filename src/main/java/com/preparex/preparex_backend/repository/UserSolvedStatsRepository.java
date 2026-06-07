package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.UserSolvedStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserSolvedStats entity.
 * Provides lookup by user ID for profile stats display.
 */
@Repository
public interface UserSolvedStatsRepository extends JpaRepository<UserSolvedStats, UUID> {

    Optional<UserSolvedStats> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
