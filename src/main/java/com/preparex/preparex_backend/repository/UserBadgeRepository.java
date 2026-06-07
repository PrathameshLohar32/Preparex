package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.UserBadge;
import com.preparex.preparex_backend.enums.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for UserBadge entity.
 * Supports listing earned badges and idempotent award checks.
 */
@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {

    List<UserBadge> findAllByUserId(UUID userId);

    boolean existsByUserIdAndBadgeType(UUID userId, BadgeType badgeType);
}
