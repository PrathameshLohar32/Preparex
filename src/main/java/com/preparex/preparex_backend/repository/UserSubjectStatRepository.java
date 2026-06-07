package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.UserSubjectStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserSubjectStat entity.
 * Provides per-subject accuracy data for radar chart.
 */
@Repository
public interface UserSubjectStatRepository extends JpaRepository<UserSubjectStat, UUID> {

    List<UserSubjectStat> findAllByUserId(UUID userId);

    Optional<UserSubjectStat> findByUserIdAndSubjectId(UUID userId, Integer subjectId);

    void deleteAllByUserId(UUID userId);
}
