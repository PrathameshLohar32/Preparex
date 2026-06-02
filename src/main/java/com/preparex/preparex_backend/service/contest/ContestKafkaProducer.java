package com.preparex.preparex_backend.service.contest;

import com.preparex.preparex_backend.config.ContestKafkaConfig;
import com.preparex.preparex_backend.event.ContestSubmissionEvent;
import com.preparex.preparex_backend.event.ContestEndedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka producers for contest events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContestKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishSubmission(ContestSubmissionEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ContestKafkaConfig.TOPIC_CONTEST_SUBMISSIONS,
                    event.getContestId().toString(), payload);
            log.info("Published contest submission event: contest={}, user={}, problem={}",
                    event.getContestId(), event.getUserId(), event.getProblemId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize contest submission event", e);
            throw new RuntimeException("Kafka serialization failed", e);
        }
    }

    public void publishContestEnded(UUID contestId) {
        try {
            ContestEndedEvent event = ContestEndedEvent.builder().contestId(contestId).build();
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ContestKafkaConfig.TOPIC_CONTEST_ENDED,
                    contestId.toString(), payload);
            log.info("Published contest-ended event: contestId={}", contestId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize contest-ended event", e);
            throw new RuntimeException("Kafka serialization failed", e);
        }
    }
}
