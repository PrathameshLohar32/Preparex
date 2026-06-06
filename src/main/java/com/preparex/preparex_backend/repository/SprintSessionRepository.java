package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.SprintSession;
import com.preparex.preparex_backend.enums.SprintSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SprintSession entity.
 * Provides lookup for active sessions and user session history.
 */
@Repository
public interface SprintSessionRepository extends JpaRepository<SprintSession, UUID> {

    /**
     * Finds an active sprint session for a user.
     * Used to enforce one-active-session-per-user constraint.
     */
    Optional<SprintSession> findByUserIdAndStatus(UUID userId, SprintSessionStatus status);

    /**
     * Counts total completed sprints for a user.
     * Used for user profile stats.
     */
    long countByUserIdAndStatus(UUID userId, SprintSessionStatus status);
}
