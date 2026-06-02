package com.preparex.preparex_backend.service.contest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preparex.preparex_backend.config.ContestKafkaConfig;
import com.preparex.preparex_backend.entity.Contest;
import com.preparex.preparex_backend.entity.ContestResult;
import com.preparex.preparex_backend.enums.ContestStatus;
import com.preparex.preparex_backend.event.ContestEndedEvent;
import com.preparex.preparex_backend.repository.ContestRepository;
import com.preparex.preparex_backend.repository.ContestResultRepository;
import com.preparex.preparex_backend.service.contest.result.ContestResultCalculator;
import com.preparex.preparex_backend.service.contest.result.ContestResultCalculatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kafka consumer triggered by contest-ended event.
 * Calculates final results using Template Method pattern, persists them,
 * and transitions contest to RESULTS_PUBLISHED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContestResultFinalizer {

    private final ObjectMapper objectMapper;
    private final ContestRepository contestRepository;
    private final ContestResultRepository contestResultRepository;
    private final ContestResultCalculatorFactory calculatorFactory;

    @KafkaListener(
            topics = ContestKafkaConfig.TOPIC_CONTEST_ENDED,
            groupId = "result-group"
    )
    @Transactional
    public void handleContestEnded(String payload) {
        try {
            ContestEndedEvent event = objectMapper.readValue(payload, ContestEndedEvent.class);
            log.info("Processing contest-ended event: contestId={}", event.getContestId());

            // Prevent duplicate finalization
            if (contestResultRepository.existsByContestId(event.getContestId())) {
                log.warn("Results already exist for contestId={}, skipping", event.getContestId());
                return;
            }

            Contest contest = contestRepository.findById(event.getContestId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Contest not found: " + event.getContestId()));

            // Calculate results using Template Method
            ContestResultCalculator calculator = calculatorFactory.getCalculator(contest.getType());
            List<ContestResult> results = calculator.calculate(contest);

            // Persist all results
            contestResultRepository.saveAll(results);

            // Transition to RESULTS_PUBLISHED
            contest.setStatus(ContestStatus.RESULTS_PUBLISHED);
            contestRepository.save(contest);

            log.info("Finalized {} results for contest={}, status=RESULTS_PUBLISHED",
                    results.size(), event.getContestId());

        } catch (Exception e) {
            log.error("Failed to finalize contest results: {}", e.getMessage(), e);
        }
    }
}
