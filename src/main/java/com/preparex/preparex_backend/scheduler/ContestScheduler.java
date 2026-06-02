package com.preparex.preparex_backend.scheduler;

import com.preparex.preparex_backend.entity.Contest;
import com.preparex.preparex_backend.enums.ContestStatus;
import com.preparex.preparex_backend.repository.ContestRepository;
import com.preparex.preparex_backend.service.contest.ContestKafkaProducer;
import com.preparex.preparex_backend.service.contest.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled job that manages contest state transitions.
 * Runs every 60 seconds.
 *
 * <p>Transitions:</p>
 * <ul>
 *   <li>SCHEDULED → LIVE — when starts_at <= now()</li>
 *   <li>LIVE → ENDED — when ends_at <= now(). Publishes contest-ended Kafka event.</li>
 * </ul>
 *
 * <p>Uses Redisson RLock to prevent race conditions between scheduler and admin operations.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContestScheduler {

    private final ContestRepository contestRepository;
    private final ContestKafkaProducer kafkaProducer;
    private final LeaderboardService leaderboardService;
    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "contest:state:";

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkContestTransitions() {
        Instant now = Instant.now();

        // Transition SCHEDULED → LIVE
        List<Contest> readyToStart = contestRepository
                .findScheduledReadyToStart(ContestStatus.SCHEDULED, now);

        for (Contest contest : readyToStart) {
            transitionToLive(contest);
        }

        // Transition LIVE → ENDED
        List<Contest> readyToEnd = contestRepository
                .findLiveReadyToEnd(ContestStatus.LIVE, now);

        for (Contest contest : readyToEnd) {
            transitionToEnded(contest);
        }
    }

    private void transitionToLive(Contest contest) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + contest.getId());
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                try {
                    // Re-check status after acquiring lock
                    Contest fresh = contestRepository.findById(contest.getId()).orElse(null);
                    if (fresh == null || fresh.getStatus() != ContestStatus.SCHEDULED) return;

                    fresh.setStatus(ContestStatus.LIVE);
                    fresh.setUpdatedAt(Instant.now());
                    contestRepository.save(fresh);

                    log.info("Contest {} transitioned SCHEDULED → LIVE", contest.getId());
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock interrupted for contest={}", contest.getId());
        }
    }

    private void transitionToEnded(Contest contest) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + contest.getId());
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                try {
                    Contest fresh = contestRepository.findById(contest.getId()).orElse(null);
                    if (fresh == null || fresh.getStatus() != ContestStatus.LIVE) return;

                    fresh.setStatus(ContestStatus.ENDED);
                    fresh.setUpdatedAt(Instant.now());
                    contestRepository.save(fresh);

                    // Publish contest-ended Kafka event for result finalization
                    kafkaProducer.publishContestEnded(fresh.getId());

                    log.info("Contest {} transitioned LIVE → ENDED, published contest-ended event",
                            contest.getId());
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock interrupted for contest={}", contest.getId());
        }
    }
}
