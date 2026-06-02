package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.ContestRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContestRegistrationRepository extends JpaRepository<ContestRegistration, Long> {

    Optional<ContestRegistration> findByContestIdAndUserId(UUID contestId, UUID userId);

    boolean existsByContestIdAndUserId(UUID contestId, UUID userId);

    long countByContestId(UUID contestId);

    List<ContestRegistration> findByContestId(UUID contestId);
}
