package com.preparex.preparex_backend.event;

import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.entity.Submission;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.entity.UserStreak;
import com.preparex.preparex_backend.enums.SubmissionSource;
import com.preparex.preparex_backend.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Listens for SubmissionSavedEvent to update user streaks.
 * Only processes DAILY-source submissions. Runs asynchronously
 * so the HTTP response is never delayed.
 *
 * <p><strong>Streak logic:</strong></p>
 * <ul>
 *   <li>lastActiveDate == today → do nothing (already counted)</li>
 *   <li>lastActiveDate == yesterday → currentStreak++</li>
 *   <li>lastActiveDate &lt; yesterday OR null → currentStreak = 1</li>
 *   <li>Update longestStreak if currentStreak > longestStreak</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreakEventListener {

    private final UserStreakRepository userStreakRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Async
    @EventListener
    @Transactional
    public void handleSubmissionSaved(SubmissionSavedEvent event) {
        Submission submission = event.getSubmission();

        if (submission.getSource() != SubmissionSource.DAILY) {
            return;
        }

        User user = submission.getUser();
        LocalDate today = LocalDate.now();

        log.info("Processing streak update for userId={}, date={}", user.getId(), today);

        UserStreak streak = userStreakRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Creating new streak record for userId={}", user.getId());
                    return UserStreak.builder()
                            .user(user)
                            .currentStreak(0)
                            .longestStreak(0)
                            .build();
                });

        LocalDate lastActive = streak.getLastActiveDate();

        if (lastActive != null && lastActive.equals(today)) {
            log.debug("Streak already counted for userId={} on {}", user.getId(), today);
            return;
        }

        if (lastActive != null && lastActive.equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1);
        }

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        streak.setLastActiveDate(today);
        streak.setUpdatedAt(Instant.now());

        userStreakRepository.save(streak);

        // Evict cached streak data
        String cacheKey = RedisKeyConstants.streakCacheKey(user.getId().toString());
        redisTemplate.delete(cacheKey);

        log.info("Updated streak for userId={}: current={}, longest={}",
                user.getId(), streak.getCurrentStreak(), streak.getLongestStreak());
    }
}
