package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.config.ContestKafkaConfig;
import com.preparex.preparex_backend.entity.UserBadge;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.enums.BadgeType;
import com.preparex.preparex_backend.event.BadgeAwardedKafkaEvent;
import com.preparex.preparex_backend.repository.UserBadgeRepository;
import com.preparex.preparex_backend.repository.UserRepository;
import com.preparex.preparex_backend.service.BadgeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Badge service implementation with idempotent award logic.
 * Awards are persisted to user_badges and published to Kafka badge-events topic.
 * Threshold-based checks automatically award badges when milestones are hit.
 */
@Slf4j
@Service
public class BadgeServiceImpl implements BadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public BadgeServiceImpl(UserBadgeRepository userBadgeRepository,
                            UserRepository userRepository,
                            KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapper objectMapper) {
        this.userBadgeRepository = userBadgeRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void award(UUID userId, BadgeType type, String context) {
        // Idempotent — skip if already awarded
        if (userBadgeRepository.existsByUserIdAndBadgeType(userId, type)) {
            log.debug("Badge {} already awarded to user {}", type, userId);
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot award badge {} — user {} not found", type, userId);
            return;
        }

        UserBadge badge = UserBadge.builder()
                .user(user)
                .badgeType(type)
                .context(context)
                .build();
        userBadgeRepository.save(badge);

        log.info("Badge awarded: type={}, userId={}, context={}", type, userId, context);

        // Publish to Kafka for other consumers
        publishBadgeEvent(userId, type, context);
    }

    @Override
    public void checkAndAwardStreakBadges(UUID userId, int currentStreak) {
        if (currentStreak >= 7) {
            award(userId, BadgeType.STREAK_7, "7-day streak achieved");
        }
        if (currentStreak >= 30) {
            award(userId, BadgeType.STREAK_30, "30-day streak achieved");
        }
        if (currentStreak >= 100) {
            award(userId, BadgeType.STREAK_100, "100-day streak achieved");
        }
    }

    @Override
    public void checkAndAwardSolvedBadges(UUID userId, int totalSolved) {
        if (totalSolved >= 50) {
            award(userId, BadgeType.SOLVED_50, "Solved 50 problems");
        }
        if (totalSolved >= 100) {
            award(userId, BadgeType.SOLVED_100, "Solved 100 problems");
        }
        if (totalSolved >= 500) {
            award(userId, BadgeType.SOLVED_500, "Solved 500 problems");
        }
    }

    @Override
    public void checkAndAwardContestBadges(UUID userId, UUID contestId, int rank, double percentile) {
        // Participation badge
        award(userId, BadgeType.CONTEST_PARTICIPANT,
                "Participated in contest " + contestId);

        // Top 10% badge
        if (percentile >= 90.0) {
            award(userId, BadgeType.CONTEST_TOP_10_PERCENT,
                    "Top 10% in contest " + contestId);
        }

        // Winner badge
        if (rank == 1) {
            award(userId, BadgeType.CONTEST_WINNER,
                    "Won contest " + contestId);
        }
    }

    /**
     * Publishes a badge awarded event to Kafka for downstream consumers.
     */
    private void publishBadgeEvent(UUID userId, BadgeType type, String context) {
        try {
            BadgeAwardedKafkaEvent event = BadgeAwardedKafkaEvent.builder()
                    .userId(userId)
                    .badgeType(type.name())
                    .context(context)
                    .build();

            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ContestKafkaConfig.TOPIC_BADGE_EVENTS, userId.toString(), json);
        } catch (Exception e) {
            log.error("Failed to publish badge event for user {}: {}", userId, e.getMessage());
        }
    }
}
