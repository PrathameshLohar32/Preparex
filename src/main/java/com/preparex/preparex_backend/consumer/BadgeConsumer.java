package com.preparex.preparex_backend.consumer;

import com.preparex.preparex_backend.config.ContestKafkaConfig;
import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.entity.UserBadge;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.enums.BadgeType;
import com.preparex.preparex_backend.event.BadgeAwardedKafkaEvent;
import com.preparex.preparex_backend.repository.UserBadgeRepository;
import com.preparex.preparex_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Kafka consumer for badge-events.
 * Persists badges to user_badges with idempotent INSERT (UNIQUE constraint as safety net).
 * Evicts Redis profile badge cache.
 */
@Slf4j
@Component
public class BadgeConsumer {

    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public BadgeConsumer(UserBadgeRepository userBadgeRepository,
                         UserRepository userRepository,
                         ObjectMapper objectMapper,
                         RedisTemplate<String, Object> redisTemplate) {
        this.userBadgeRepository = userBadgeRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = ContestKafkaConfig.TOPIC_BADGE_EVENTS, groupId = "badge-pg")
    @Transactional
    public void onBadgeAwarded(String message) {
        try {
            BadgeAwardedKafkaEvent event = objectMapper.readValue(message, BadgeAwardedKafkaEvent.class);
            UUID userId = event.getUserId();
            BadgeType badgeType = BadgeType.valueOf(event.getBadgeType());

            // Idempotent — skip if already exists
            if (userBadgeRepository.existsByUserIdAndBadgeType(userId, badgeType)) {
                log.debug("Badge {} already exists for user {}", badgeType, userId);
                return;
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("Cannot persist badge — user {} not found", userId);
                return;
            }

            UserBadge badge = UserBadge.builder()
                    .user(user)
                    .badgeType(badgeType)
                    .context(event.getContext())
                    .build();
            userBadgeRepository.save(badge);

            log.info("Badge persisted via Kafka: type={}, userId={}", badgeType, userId);

            // Evict badge cache
            String uid = userId.toString();
            redisTemplate.delete(List.of(
                    RedisKeyConstants.profileBadgesKey(uid),
                    RedisKeyConstants.profileFullKey(uid)
            ));

        } catch (Exception e) {
            log.error("Error processing badge-events: {}", e.getMessage(), e);
        }
    }
}
