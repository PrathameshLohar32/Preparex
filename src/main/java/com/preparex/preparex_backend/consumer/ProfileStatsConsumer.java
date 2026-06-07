package com.preparex.preparex_backend.consumer;

import com.preparex.preparex_backend.config.ProfileKafkaConfig;
import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.entity.UserSolvedStats;
import com.preparex.preparex_backend.entity.UserSubjectStat;
import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.event.SubmissionSavedKafkaEvent;
import com.preparex.preparex_backend.repository.SubjectRepository;
import com.preparex.preparex_backend.repository.UserRepository;
import com.preparex.preparex_backend.repository.UserSolvedStatsRepository;
import com.preparex.preparex_backend.repository.UserSubjectStatRepository;
import com.preparex.preparex_backend.service.BadgeService;
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
 * Kafka consumer for submission-saved events.
 * Updates UserSolvedStats and UserSubjectStat asynchronously.
 * Evicts Redis profile caches on each update.
 */
@Slf4j
@Component
public class ProfileStatsConsumer {

    private final UserSolvedStatsRepository userSolvedStatsRepository;
    private final UserSubjectStatRepository userSubjectStatRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final BadgeService badgeService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public ProfileStatsConsumer(UserSolvedStatsRepository userSolvedStatsRepository,
                                UserSubjectStatRepository userSubjectStatRepository,
                                UserRepository userRepository,
                                SubjectRepository subjectRepository,
                                BadgeService badgeService,
                                ObjectMapper objectMapper,
                                RedisTemplate<String, Object> redisTemplate) {
        this.userSolvedStatsRepository = userSolvedStatsRepository;
        this.userSubjectStatRepository = userSubjectStatRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.badgeService = badgeService;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = ProfileKafkaConfig.TOPIC_SUBMISSION_SAVED, groupId = "profile-stats-pg")
    @Transactional
    public void onSubmissionSaved(String message) {
        try {
            SubmissionSavedKafkaEvent event = objectMapper.readValue(message, SubmissionSavedKafkaEvent.class);
            UUID userId = event.getUserId();
            boolean isCorrect = "CORRECT".equals(event.getStatus());

            log.debug("Processing submission event: userId={}, status={}, difficulty={}",
                    userId, event.getStatus(), event.getDifficulty());

            // Update UserSolvedStats
            UserSolvedStats stats = userSolvedStatsRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        User user = userRepository.getReferenceById(userId);
                        return UserSolvedStats.builder().user(user).build();
                    });

            if (isCorrect) {
                stats.setTotal(stats.getTotal() + 1);

                if (event.getDifficulty() != null) {
                    try {
                        Difficulty difficulty = Difficulty.valueOf(event.getDifficulty());
                        switch (difficulty) {
                            case EASY -> stats.setEasy(stats.getEasy() + 1);
                            case MEDIUM -> stats.setMedium(stats.getMedium() + 1);
                            case HARD -> stats.setHard(stats.getHard() + 1);
                        }
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown difficulty: {}", event.getDifficulty());
                    }
                }
            }

            stats.setUpdatedAt(Instant.now());
            userSolvedStatsRepository.save(stats);

            // Update UserSubjectStat
            if (event.getSubjectId() != null) {
                UserSubjectStat subjectStat = userSubjectStatRepository
                        .findByUserIdAndSubjectId(userId, event.getSubjectId())
                        .orElseGet(() -> {
                            User user = userRepository.getReferenceById(userId);
                            return UserSubjectStat.builder()
                                    .user(user)
                                    .subject(subjectRepository.getReferenceById(event.getSubjectId()))
                                    .build();
                        });

                subjectStat.setAttempted(subjectStat.getAttempted() + 1);
                if (isCorrect) {
                    subjectStat.setSolved(subjectStat.getSolved() + 1);
                }

                // Recalculate accuracy
                if (subjectStat.getAttempted() > 0) {
                    subjectStat.setAccuracy(
                            (subjectStat.getSolved() * 100.0) / subjectStat.getAttempted());
                }

                subjectStat.setUpdatedAt(Instant.now());
                userSubjectStatRepository.save(subjectStat);
            }

            // Check badge milestones
            if (isCorrect) {
                badgeService.checkAndAwardSolvedBadges(userId, stats.getTotal());
            }

            // Evict Redis caches
            evictProfileCaches(userId);

        } catch (Exception e) {
            log.error("Error processing submission-saved event: {}", e.getMessage(), e);
        }
    }

    private void evictProfileCaches(UUID userId) {
        String uid = userId.toString();
        redisTemplate.delete(List.of(
                RedisKeyConstants.profileStatsKey(uid),
                RedisKeyConstants.profileSubjectKey(uid),
                RedisKeyConstants.profileFullKey(uid)
        ));
    }
}
