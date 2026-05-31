package com.preparex.preparex_backend.service.strategy.scoring;

import com.preparex.preparex_backend.enums.QuestionType;
import com.preparex.preparex_backend.service.strategy.ScoringResult;
import com.preparex.preparex_backend.service.strategy.ScoringStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Scoring strategy for MCQ_MULTIPLE questions.
 * Full marks only if the selected set EXACTLY matches the correct set.
 * Partial marks if partial scoring is enabled in answer_key.
 */
@Slf4j
@Component
public class McqMultipleScoringStrategy implements ScoringStrategy {

    private static final int FULL_MARKS = 4;
    private static final int PARTIAL_MARKS = 2;

    @Override
    public QuestionType getSupportedType() {
        return QuestionType.MCQ_MULTIPLE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ScoringResult score(Map<String, Object> submittedAnswer, Map<String, Object> answerKey) {
        List<String> submitted = toStringList(submittedAnswer.get("selected"));
        List<String> correct = toStringList(answerKey.get("correct"));

        if (submitted.isEmpty()) {
            return ScoringResult.builder()
                    .correct(false)
                    .marksAwarded(0)
                    .explanation("No options selected")
                    .build();
        }

        Set<String> submittedSet = new HashSet<>(submitted);
        Set<String> correctSet = new HashSet<>(correct);

        boolean exactMatch = submittedSet.equals(correctSet);

        if (exactMatch) {
            return ScoringResult.builder()
                    .correct(true)
                    .marksAwarded(FULL_MARKS)
                    .explanation("All correct options selected!")
                    .build();
        }

        // Check for partial scoring
        boolean partialEnabled = Boolean.TRUE.equals(answerKey.get("partialScoring"));
        boolean hasWrongSelections = !correctSet.containsAll(submittedSet);

        if (partialEnabled && !hasWrongSelections && !submittedSet.isEmpty()) {
            return ScoringResult.builder()
                    .correct(false)
                    .marksAwarded(PARTIAL_MARKS)
                    .explanation("Partially correct — some correct options selected, none wrong")
                    .build();
        }

        return ScoringResult.builder()
                .correct(false)
                .marksAwarded(0)
                .explanation("Incorrect selection")
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        return List.of();
    }
}
