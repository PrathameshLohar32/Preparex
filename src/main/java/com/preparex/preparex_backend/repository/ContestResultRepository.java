package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.ContestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContestResultRepository extends JpaRepository<ContestResult, UUID> {

    Optional<ContestResult> findByContestIdAndUserId(UUID contestId, UUID userId);

    Page<ContestResult> findByContestIdOrderByRankAsc(UUID contestId, Pageable pageable);

    boolean existsByContestId(UUID contestId);
}
