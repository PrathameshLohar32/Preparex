package com.preparex.preparex_backend.consumer;

import com.preparex.preparex_backend.config.ProfileKafkaConfig;
import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.entity.UserSprintStats;
import com.preparex.preparex_backend.event.SprintEndedKafkaEvent;
import com.preparex.preparex_backend.repository.UserRepository;
import com.preparex.preparex_backend.repository.UserSprintStatsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Kafka consumer for sprint-ended events.
 * Updates UserSprintStats with accumulated totals and best rank.
 * Evicts Redis profile sprint cache.
 */
@Slf4j
@Component
public class SprintStatsConsumer {

    private final UserSprintStatsRepository userSprintStatsRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public SprintStatsConsumer(UserSprintStatsRepository userSprintStatsRepository,
                               UserRepository userRepository,
                               ObjectMapper objectMapper,
                               RedisTemplate<String, Object> redisTemplate) {
        this.userSprintStatsRepository = userSprintStatsRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = ProfileKafkaConfig.TOPIC_SPRINT_ENDED, groupId = "sprint-stats-pg")
    @Transactional
    public void onSprintEnded(String message) {
        try {
            SprintEndedKafkaEvent event = objectMapper.readValue(message, SprintEndedKafkaEvent.class);
            UUID userId = event.getUserId();

            log.debug("Processing sprint-ended event: userId={}, points={}", userId, event.getSprintPoints());

            UserSprintStats stats = userSprintStatsRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        User user = userRepository.getReferenceById(userId);
                        return UserSprintStats.builder().user(user).build();
                    });

            stats.setTotalSprints(stats.getTotalSprints() + 1);
            stats.setTotalPoints(stats.getTotalPoints() + event.getSprintPoints());

            // Update best weekly rank if improved
            if (event.getWeeklyRank() != null) {
                if (stats.getBestWeeklyRank() == null || event.getWeeklyRank() < stats.getBestWeeklyRank()) {
                    stats.setBestWeeklyRank(event.getWeeklyRank());
                }
            }

            stats.setUpdatedAt(Instant.now());
            userSprintStatsRepository.save(stats);

            log.info("Sprint stats updated: userId={}, totalSprints={}, totalPoints={}",
                    userId, stats.getTotalSprints(), stats.getTotalPoints());

            // Evict sprint cache
            String uid = userId.toString();
            redisTemplate.delete(List.of(
                    RedisKeyConstants.profileSprintKey(uid),
                    RedisKeyConstants.profileFullKey(uid)
            ));

        } catch (Exception e) {
            log.error("Error processing sprint-ended event: {}", e.getMessage(), e);
        }
    }
}
