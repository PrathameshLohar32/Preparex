package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.UserSprintStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserSprintStats entity.
 * Provides aggregate sprint metrics per user.
 */
@Repository
public interface UserSprintStatsRepository extends JpaRepository<UserSprintStats, UUID> {

    Optional<UserSprintStats> findByUserId(UUID userId);
}
