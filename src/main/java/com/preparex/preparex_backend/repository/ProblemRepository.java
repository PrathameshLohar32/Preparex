package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.Problem;
import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Problem entity.
 * Provides dynamic filtering via JPQL with optional parameters,
 * slug-based lookup, and passage (parent-child) queries.
 */
@Repository
public interface ProblemRepository extends JpaRepository<Problem, UUID> {

    /**
     * Dynamic filter query for problem listing.
     * All filter parameters are optional — null values are ignored.
     * Only returns active, non-child problems (parent_id IS NULL).
     */
    @Query("""
            SELECT p FROM Problem p
            LEFT JOIN FETCH p.subject s
            LEFT JOIN FETCH p.topic t
            WHERE p.isActive = true
              AND p.parent IS NULL
              AND (:subjectId IS NULL OR p.subject.id = :subjectId)
              AND (:topicId IS NULL OR p.topic.id = :topicId)
              AND (:difficulty IS NULL OR p.difficulty = :difficulty)
              AND (:questionType IS NULL OR p.questionType = :questionType)
              AND (:examId IS NULL OR p.examId = :examId)
              AND (:pyqOnly = false OR p.pyqYear IS NOT NULL)
            """)
    Page<Problem> findByFilters(
            @Param("subjectId") Integer subjectId,
            @Param("topicId") Integer topicId,
            @Param("difficulty") Difficulty difficulty,
            @Param("questionType") QuestionType questionType,
            @Param("examId") String examId,
            @Param("pyqOnly") boolean pyqOnly,
            Pageable pageable
    );

    Optional<Problem> findBySlugAndIsActiveTrue(String slug);

    Optional<Problem> findByIdAndIsActiveTrue(UUID id);

    /**
     * Finds all child problems for a passage-type parent.
     */
    List<Problem> findByParentIdAndIsActiveTrueOrderBySlugAsc(UUID parentId);

    long countByIsActiveTrue();
}
