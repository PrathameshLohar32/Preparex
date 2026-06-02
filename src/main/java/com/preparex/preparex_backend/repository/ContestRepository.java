package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.Contest;
import com.preparex.preparex_backend.enums.ContestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContestRepository extends JpaRepository<Contest, UUID> {

    Page<Contest> findByStatusInOrderByStartsAtDesc(List<ContestStatus> statuses, Pageable pageable);

    Page<Contest> findByStatusOrderByStartsAtDesc(ContestStatus status, Pageable pageable);

    @Query("SELECT c FROM Contest c WHERE c.status = :status AND c.startsAt <= :now")
    List<Contest> findScheduledReadyToStart(@Param("status") ContestStatus status, @Param("now") Instant now);

    @Query("SELECT c FROM Contest c WHERE c.status = :status AND c.endsAt <= :now")
    List<Contest> findLiveReadyToEnd(@Param("status") ContestStatus status, @Param("now") Instant now);

    @Query("SELECT c FROM Contest c WHERE c.status = 'SCHEDULED' AND c.startsAt BETWEEN :from AND :to")
    List<Contest> findUpcomingForReminder(@Param("from") Instant from, @Param("to") Instant to);
}
