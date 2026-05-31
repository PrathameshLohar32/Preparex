package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.ProblemSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ProblemSet entity.
 * Supports slug-based lookup and active-only listings.
 */
@Repository
public interface ProblemSetRepository extends JpaRepository<ProblemSet, UUID> {

    Optional<ProblemSet> findBySlugAndIsActiveTrue(String slug);

    List<ProblemSet> findAllByIsActiveTrueOrderByDisplayOrderAsc();

    boolean existsBySlug(String slug);
}
