package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Topic entity.
 * Supports lookup by subject ID for filtered topic listings.
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> {

    List<Topic> findBySubjectIdOrderByDisplayOrderAsc(Integer subjectId);

    List<Topic> findAllByOrderByDisplayOrderAsc();
}
