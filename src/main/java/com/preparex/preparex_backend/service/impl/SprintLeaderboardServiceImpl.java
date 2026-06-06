package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.constant.SprintConstants;
import com.preparex.preparex_backend.dto.response.SprintLeaderboardEntryDto;
import com.preparex.preparex_backend.dto.response.SprintRankDto;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.repository.UserRepository;
import com.preparex.preparex_backend.service.SprintLeaderboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Sprint leaderboard service using Redis ZSets for real-time rankings.
 * Maintains both weekly (YYYY-WW) and monthly (YYYY-MM) leaderboards.
 *
 * <p>Points are accumulated via ZADD INCR across multiple sprint sessions
 * within the same period. Keys auto-expire via TTL.</p>
 */
@Slf4j
@Service
public class SprintLeaderboardServiceImpl implements SprintLeaderboardService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    public SprintLeaderboardServiceImpl(RedisTemplate<String, Object> redisTemplate,
                                        UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    @Override
    public void addPoints(UUID userId, int points) {
        String weekKey = getCurrentWeekKey();
        String monthKey = getCurrentMonthKey();
        String userIdStr = userId.toString();

        String weekRedisKey = RedisKeyConstants.sprintWeeklyLeaderboardKey(weekKey);
        String monthRedisKey = RedisKeyConstants.sprintMonthlyLeaderboardKey(monthKey);

        redisTemplate.opsForZSet().incrementScore(weekRedisKey, userIdStr, points);
        redisTemplate.opsForZSet().incrementScore(monthRedisKey, userIdStr, points);

        // Set TTL only if the key is newly created (TTL = -1 means no expiry set)
        setTtlIfAbsent(weekRedisKey, SprintConstants.WEEKLY_LEADERBOARD_TTL_DAYS);
        setTtlIfAbsent(monthRedisKey, SprintConstants.MONTHLY_LEADERBOARD_TTL_DAYS);

        log.info("Added {} sprint points for userId={} to weekly={} and monthly={}",
                points, userId, weekKey, monthKey);
    }

    @Override
    public List<SprintLeaderboardEntryDto> getWeeklyTop(int limit) {
        String weekKey = getCurrentWeekKey();
        String redisKey = RedisKeyConstants.sprintWeeklyLeaderboardKey(weekKey);
        return getTopEntries(redisKey, limit);
    }

    @Override
    public List<SprintLeaderboardEntryDto> getMonthlyTop(int limit) {
        String monthKey = getCurrentMonthKey();
        String redisKey = RedisKeyConstants.sprintMonthlyLeaderboardKey(monthKey);
        return getTopEntries(redisKey, limit);
    }

    @Override
    public SprintRankDto getUserWeeklyRank(UUID userId) {
        String weekKey = getCurrentWeekKey();
        String redisKey = RedisKeyConstants.sprintWeeklyLeaderboardKey(weekKey);
        String userIdStr = userId.toString();

        Long rank = redisTemplate.opsForZSet().reverseRank(redisKey, userIdStr);
        Double score = redisTemplate.opsForZSet().score(redisKey, userIdStr);
        Long totalParticipants = redisTemplate.opsForZSet().zCard(redisKey);

        return SprintRankDto.builder()
                .rank(rank != null ? rank.intValue() + 1 : 0)
                .points(score != null ? score.intValue() : 0)
                .totalParticipants(totalParticipants != null ? totalParticipants : 0)
                .build();
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    private List<SprintLeaderboardEntryDto> getTopEntries(String redisKey, int limit) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, 0, (long) limit - 1);

        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<SprintLeaderboardEntryDto> entries = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            UUID userId = UUID.fromString(tuple.getValue().toString());
            String username = resolveUsername(userId);

            entries.add(SprintLeaderboardEntryDto.builder()
                    .rank(rank++)
                    .userId(userId)
                    .username(username)
                    .points(tuple.getScore() != null ? tuple.getScore().intValue() : 0)
                    .build());
        }

        return entries;
    }

    /**
     * Resolves a user's display name from the database.
     * Falls back to "Unknown" if the user is not found.
     */
    private String resolveUsername(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("Unknown");
    }

    /**
     * Sets TTL on a Redis key only if no TTL is currently set.
     * Prevents resetting TTL on every ZADD INCR call.
     */
    private void setTtlIfAbsent(String redisKey, int ttlDays) {
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        if (ttl != null && ttl == -1) {
            redisTemplate.expire(redisKey, ttlDays, TimeUnit.DAYS);
        }
    }

    private String getCurrentWeekKey() {
        LocalDate now = LocalDate.now();
        int weekNumber = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = now.get(IsoFields.WEEK_BASED_YEAR);
        return String.format("%d-W%02d", year, weekNumber);
    }

    private String getCurrentMonthKey() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
