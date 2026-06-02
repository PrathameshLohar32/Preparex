package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.ContestSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContestSubmissionRepository extends JpaRepository<ContestSubmission, UUID> {

    List<ContestSubmission> findByContestIdAndUserId(UUID contestId, UUID userId);

    List<ContestSubmission> findByContestId(UUID contestId);

    Optional<ContestSubmission> findByContestIdAndUserIdAndProblemId(UUID contestId, UUID userId, UUID problemId);

    boolean existsByContestIdAndUserIdAndProblemId(UUID contestId, UUID userId, UUID problemId);

    long countByContestIdAndUserId(UUID contestId, UUID userId);
}
