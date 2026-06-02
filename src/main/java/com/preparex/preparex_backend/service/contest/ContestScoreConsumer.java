package com.preparex.preparex_backend.service.contest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preparex.preparex_backend.config.ContestKafkaConfig;
import com.preparex.preparex_backend.entity.ContestSubmission;
import com.preparex.preparex_backend.event.ContestSubmissionEvent;
import com.preparex.preparex_backend.repository.ContestSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka consumer that scores contest submissions asynchronously.
 * Flow: Score answer → update DB → update Redis leaderboard → broadcast via WebSocket.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContestScoreConsumer {

    private final ObjectMapper objectMapper;
    private final ContestScoringService contestScoringService;
    private final ContestSubmissionRepository submissionRepository;
    private final LeaderboardService leaderboardService;
    private final LeaderboardBroadcaster leaderboardBroadcaster;

    @KafkaListener(
            topics = ContestKafkaConfig.TOPIC_CONTEST_SUBMISSIONS,
            groupId = "scoring-group"
    )
    @Transactional
    public void handleSubmission(String payload) {
        try {
            ContestSubmissionEvent event = objectMapper.readValue(payload, ContestSubmissionEvent.class);

            log.info("Scoring contest submission: contest={}, user={}, problem={}",
                    event.getContestId(), event.getUserId(), event.getProblemId());

            // 1. Score the submission
            ContestScoringService.ContestScoringResult result = contestScoringService.score(
                    event.getContestId(), event.getProblemId(), event.getAnswer());

            // 2. Update submission in DB
            ContestSubmission submission = submissionRepository.findById(event.getSubmissionId())
                    .orElseThrow(() -> new IllegalStateException(
                            "ContestSubmission not found: " + event.getSubmissionId()));

            submission.setStatus(result.status());
            submission.setMarksAwarded(result.marksAwarded());
            submissionRepository.save(submission);

            // 3. Update Redis leaderboard
            leaderboardService.incrementScore(
                    event.getContestId(), event.getUserId(), result.marksAwarded());

            // 4. Broadcast updated leaderboard via WebSocket
            var top50 = leaderboardService.getTop(event.getContestId(), 50);
            leaderboardBroadcaster.broadcastUpdate(event.getContestId(), top50);

            log.info("Scored and broadcast: contest={}, user={}, status={}, marks={}",
                    event.getContestId(), event.getUserId(), result.status(), result.marksAwarded());

        } catch (Exception e) {
            log.error("Failed to process contest submission event: {}", e.getMessage(), e);
        }
    }
}
