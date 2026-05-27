package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.SessionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionHistoryRepository extends JpaRepository<SessionHistory, UUID> {

    List<SessionHistory> findByUserIdOrderByLoggedInAtDesc(UUID userId);

    Optional<SessionHistory> findBySessionId(String sessionId);
}
