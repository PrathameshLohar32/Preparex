package com.preparex.preparex_backend.scheduler;

import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.entity.DailyProblem;
import com.preparex.preparex_backend.entity.Problem;
import com.preparex.preparex_backend.entity.Subject;
import com.preparex.preparex_backend.repository.DailyProblemRepository;
import com.preparex.preparex_backend.repository.ProblemRepository;
import com.preparex.preparex_backend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Scheduler that runs at midnight to pick daily challenge problems.
 * Selects one problem per subject (Physics, Chemistry, Maths) for the next day.
 * Avoids repeating previously used daily problems.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyProblemScheduler {

    private final DailyProblemRepository dailyProblemRepository;
    private final ProblemRepository problemRepository;
    private final SubjectRepository subjectRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Runs daily at midnight IST to populate next day's problems.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void scheduleNextDayProblems() {
        LocalDate nextDay = LocalDate.now().plusDays(1);

        if (dailyProblemRepository.existsByScheduledDate(nextDay)) {
            log.info("Daily problems already scheduled for {}, skipping", nextDay);
            return;
        }

        log.info("Scheduling daily problems for date={}", nextDay);

        List<Subject> subjects = subjectRepository.findAllByOrderByDisplayOrderAsc();

        for (Subject subject : subjects) {
            try {
                pickProblemForSubject(subject, nextDay);
            } catch (Exception e) {
                log.error("Failed to pick daily problem for subject={}, date={}",
                        subject.getName(), nextDay, e);
            }
        }

        // Evict the daily:today cache so next day's GET picks up new problems
        redisTemplate.delete(RedisKeyConstants.DAILY_TODAY_KEY);
        log.info("Daily problems scheduled for {} and cache evicted", nextDay);
    }

    /**
     * Picks one unused active problem for the given subject and date.
     * Falls back to any active problem if all have been used.
     */
    private void pickProblemForSubject(Subject subject, LocalDate date) {
        List<UUID> usedProblemIds = dailyProblemRepository
                .findUsedProblemIdsBySubjectId(subject.getId());

        // Find an active problem for this subject that hasn't been used yet
        List<Problem> candidates = problemRepository.findByFilters(
                subject.getId(),
                null, null, null,
                null, false,
                PageRequest.of(0, 50)
        ).getContent();

        Problem selected = candidates.stream()
                .filter(p -> !usedProblemIds.contains(p.getId()))
                .findFirst()
                .orElse(candidates.isEmpty() ? null : candidates.get(0));

        if (selected == null) {
            log.warn("No problems available for subject={}, skipping", subject.getName());
            return;
        }

        DailyProblem dailyProblem = DailyProblem.builder()
                .problem(selected)
                .subject(subject)
                .scheduledDate(date)
                .build();

        dailyProblemRepository.save(dailyProblem);
        log.info("Scheduled problem slug={} for subject={} on {}",
                selected.getSlug(), subject.getName(), date);
    }
}
