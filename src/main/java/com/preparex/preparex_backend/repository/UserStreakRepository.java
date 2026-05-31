package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserStreak entity.
 * One-to-one relationship with User — at most one streak row per user.
 */
@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, UUID> {

    Optional<UserStreak> findByUserId(UUID userId);
}
