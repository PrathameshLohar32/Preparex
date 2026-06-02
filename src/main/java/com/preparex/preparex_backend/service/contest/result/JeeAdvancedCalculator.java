package com.preparex.preparex_backend.service.contest.result;

import com.preparex.preparex_backend.entity.ContestProblem;
import com.preparex.preparex_backend.entity.ContestSubmission;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import com.preparex.preparex_backend.repository.ContestProblemRepository;
import com.preparex.preparex_backend.repository.ContestRegistrationRepository;
import com.preparex.preparex_backend.repository.ContestSubmissionRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * JEE Advanced marking: multi-correct with partial scoring and negative for wrong.
 * Correct = full marks, Partial = partial marks, Wrong = negative marks.
 */
@Slf4j
public class JeeAdvancedCalculator extends ContestResultCalculator {

    public JeeAdvancedCalculator(
            ContestSubmissionRepository submissionRepository,
            ContestRegistrationRepository registrationRepository,
            ContestProblemRepository problemRepository) {
        super(submissionRepository, registrationRepository, problemRepository);
    }

    @Override
    protected ParticipantScore applyMarkingScheme(
            UUID userId,
            List<ContestSubmission> submissions,
            List<ContestProblem> contestProblems,
            int totalProblems) {

        int totalScore = 0, correct = 0, wrong = 0, partial = 0, totalTime = 0;
        Set<UUID> attemptedProblems = new HashSet<>();

        for (ContestSubmission sub : submissions) {
            attemptedProblems.add(sub.getProblem().getId());
            if (sub.getTimeTakenSecs() != null) totalTime += sub.getTimeTakenSecs();

            int marks = sub.getMarksAwarded() != null ? sub.getMarksAwarded() : 0;
            totalScore += marks;

            if (sub.getStatus() == SubmissionStatus.CORRECT) {
                correct++;
            } else if (sub.getStatus() == SubmissionStatus.PARTIAL) {
                partial++;
            } else if (sub.getStatus() == SubmissionStatus.WRONG) {
                wrong++;
            }
        }

        return ParticipantScore.builder()
                .userId(userId)
                .totalScore(totalScore)
                .correctCount(correct)
                .wrongCount(wrong)
                .unattemptedCount(totalProblems - attemptedProblems.size())
                .timeTakenSecs(totalTime)
                .subjectBreakdown(Map.of("partialCount", partial))
                .build();
    }
}
