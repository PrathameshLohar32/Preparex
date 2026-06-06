package com.preparex.preparex_backend.scheduler;

import com.preparex.preparex_backend.constant.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.Set;

/**
 * Weekly scheduler for sprint leaderboard reset operations.
 * Runs every Monday at midnight to snapshot the previous week's champion.
 *
 * <p>The previous week's ZSet key auto-expires via TTL (set in SprintLeaderboardServiceImpl),
 * so no explicit deletion is needed. This scheduler only handles champion recognition.</p>
 */
@Slf4j
@Component
public class SprintWeeklyResetScheduler {

    private final RedisTemplate<String, Object> redisTemplate;

    public SprintWeeklyResetScheduler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Runs every Monday at midnight (server time).
     * Looks up the rank-1 user from the previous week's sprint leaderboard
     * and logs the champion. Badge awarding will be integrated in Phase 5.
     */
    @Scheduled(cron = "0 0 0 * * MON")
    public void processWeeklyChampion() {
        String previousWeekKey = getPreviousWeekKey();
        String redisKey = RedisKeyConstants.sprintWeeklyLeaderboardKey(previousWeekKey);

        log.info("Processing sprint weekly champion for week: {}", previousWeekKey);

        Set<ZSetOperations.TypedTuple<Object>> topOne =
                redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, 0, 0);

        if (topOne == null || topOne.isEmpty()) {
            log.info("No sprint participants found for week: {}", previousWeekKey);
            return;
        }

        ZSetOperations.TypedTuple<Object> champion = topOne.iterator().next();
        String championUserId = champion.getValue() != null ? champion.getValue().toString() : "unknown";
        int championPoints = champion.getScore() != null ? champion.getScore().intValue() : 0;

        log.info("Sprint Champion for week {}: userId={}, points={}",
                previousWeekKey, championUserId, championPoints);

        // TODO Phase 5: Award SPRINT_CHAMPION_WEEKLY badge via BadgeService
        // badgeService.awardBadge(UUID.fromString(championUserId), BadgeType.SPRINT_CHAMPION_WEEKLY);
    }

    /**
     * Computes the ISO week key for the previous week (e.g., "2026-W22").
     */
    private String getPreviousWeekKey() {
        LocalDate previousWeek = LocalDate.now().minusWeeks(1);
        int weekNumber = previousWeek.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = previousWeek.get(IsoFields.WEEK_BASED_YEAR);
        return String.format("%d-W%02d", year, weekNumber);
    }
}
