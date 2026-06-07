package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for UserProfile entity.
 * Uses UUID as PK (shared with User.id via @MapsId).
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
