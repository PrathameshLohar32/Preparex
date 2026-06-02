package com.preparex.preparex_backend.service.contest;

import com.preparex.preparex_backend.dto.response.LeaderboardEntryDto;
import com.preparex.preparex_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Redis ZSet-backed leaderboard service.
 * ZADD for score updates, ZREVRANK for rank, ZREVRANGEWITHSCORES for top N.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    private static final String LEADERBOARD_PREFIX = "contest:leaderboard:";

    public void updateScore(UUID contestId, UUID userId, int score) {
        String key = LEADERBOARD_PREFIX + contestId;
        redisTemplate.opsForZSet().add(key, userId.toString(), score);
        log.debug("Updated leaderboard: contest={}, user={}, score={}", contestId, userId, score);
    }

    public void incrementScore(UUID contestId, UUID userId, int delta) {
        String key = LEADERBOARD_PREFIX + contestId;
        redisTemplate.opsForZSet().incrementScore(key, userId.toString(), delta);
        log.debug("Incremented leaderboard: contest={}, user={}, delta={}", contestId, userId, delta);
    }

    public Long getRank(UUID contestId, UUID userId) {
        String key = LEADERBOARD_PREFIX + contestId;
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId.toString());
        return rank != null ? rank + 1 : null; // 0-indexed → 1-indexed
    }

    public List<LeaderboardEntryDto> getTop(UUID contestId, int count) {
        String key = LEADERBOARD_PREFIX + contestId;
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, count - 1);

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        long rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            String userIdStr = tuple.getValue() != null ? tuple.getValue().toString() : "";
            entries.add(LeaderboardEntryDto.builder()
                    .userId(UUID.fromString(userIdStr))
                    .score(tuple.getScore() != null ? tuple.getScore().intValue() : 0)
                    .rank(rank++)
                    .build());
        }

        // Enrich with usernames
        enrichWithUsernames(entries);
        return entries;
    }

    public void deleteLeaderboard(UUID contestId) {
        redisTemplate.delete(LEADERBOARD_PREFIX + contestId);
    }

    private void enrichWithUsernames(List<LeaderboardEntryDto> entries) {
        for (LeaderboardEntryDto entry : entries) {
            try {
                userRepository.findById(entry.getUserId())
                        .ifPresent(u -> entry.setUsername(u.getUsername()));
            } catch (Exception e) {
                log.warn("Could not enrich username for userId={}", entry.getUserId());
                entry.setUsername("Unknown");
            }
        }
    }
}
