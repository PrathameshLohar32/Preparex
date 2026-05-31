package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Subject entity.
 * Supports lookup by exam ID for filtered subject listings.
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    List<Subject> findByExamIdOrderByDisplayOrderAsc(String examId);

    List<Subject> findAllByOrderByDisplayOrderAsc();
}
