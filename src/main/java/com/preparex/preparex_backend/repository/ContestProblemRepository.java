package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.ContestProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContestProblemRepository extends JpaRepository<ContestProblem, Integer> {

    List<ContestProblem> findByContestIdOrderByPositionAsc(UUID contestId);

    Optional<ContestProblem> findByContestIdAndProblemId(UUID contestId, UUID problemId);

    boolean existsByContestIdAndProblemId(UUID contestId, UUID problemId);

    long countByContestId(UUID contestId);
}
