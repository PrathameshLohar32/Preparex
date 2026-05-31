package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.enums.QuestionType;
import com.preparex.preparex_backend.service.strategy.ScoringResult;
import com.preparex.preparex_backend.service.strategy.ScoringStrategy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Scoring service that resolves the correct ScoringStrategy
 * for a given question type and delegates scoring.
 *
 * <p>Auto-discovers all ScoringStrategy @Component beans at startup
 * and builds an internal lookup map by QuestionType.</p>
 */
@Slf4j
@Service
public class ScoringService {

    private final Map<QuestionType, ScoringStrategy> strategyMap = new EnumMap<>(QuestionType.class);
    private final List<ScoringStrategy> strategies;

    public ScoringService(List<ScoringStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void init() {
        strategies.forEach(strategy -> {
            strategyMap.put(strategy.getSupportedType(), strategy);
            log.info("Registered scoring strategy: {} → {}",
                    strategy.getSupportedType(), strategy.getClass().getSimpleName());
        });

        log.info("Total scoring strategies registered: {}", strategyMap.size());
    }

    /**
     * Scores a submission using the appropriate strategy for the question type.
     *
     * @param questionType    the type of the problem
     * @param submittedAnswer the user's answer
     * @param answerKey       the correct answer (NEVER exposed to client)
     * @return scoring result
     * @throws IllegalArgumentException if no strategy exists for the question type
     */
    public ScoringResult score(QuestionType questionType,
                               Map<String, Object> submittedAnswer,
                               Map<String, Object> answerKey) {
        ScoringStrategy strategy = strategyMap.get(questionType);

        if (strategy == null) {
            log.error("No scoring strategy found for question type: {}", questionType);
            throw new IllegalArgumentException("Unsupported question type for scoring: " + questionType);
        }

        log.debug("Scoring with strategy: {} for type: {}",
                strategy.getClass().getSimpleName(), questionType);

        return strategy.score(submittedAnswer, answerKey);
    }
}
