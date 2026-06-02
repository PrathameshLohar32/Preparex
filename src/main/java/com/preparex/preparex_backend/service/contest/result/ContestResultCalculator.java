package com.preparex.preparex_backend.service.contest.result;

import com.preparex.preparex_backend.entity.*;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import com.preparex.preparex_backend.repository.ContestProblemRepository;
import com.preparex.preparex_backend.repository.ContestRegistrationRepository;
import com.preparex.preparex_backend.repository.ContestSubmissionRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Template Method abstract class for contest result calculation.
 * Subclasses override applyMarkingScheme() for contest-type-specific scoring.
 *
 * <p>Template method flow:</p>
 * <ol>
 *   <li>collectSubmissions()</li>
 *   <li>applyMarkingScheme() — abstract, varies by contest type</li>
 *   <li>computeRanks()</li>
 *   <li>computePercentiles()</li>
 *   <li>buildResults()</li>
 * </ol>
 */
@Slf4j
public abstract class ContestResultCalculator {

    protected final ContestSubmissionRepository submissionRepository;
    protected final ContestRegistrationRepository registrationRepository;
    protected final ContestProblemRepository problemRepository;

    protected ContestResultCalculator(
            ContestSubmissionRepository submissionRepository,
            ContestRegistrationRepository registrationRepository,
            ContestProblemRepository problemRepository) {
        this.submissionRepository = submissionRepository;
        this.registrationRepository = registrationRepository;
        this.problemRepository = problemRepository;
    }

    /**
     * Template method — calculates results for all participants.
     */
    public final List<ContestResult> calculate(Contest contest) {
        log.info("Calculating results for contest={}", contest.getId());

        // Step 1: Collect all submissions
        List<ContestSubmission> allSubmissions = submissionRepository.findByContestId(contest.getId());
        List<ContestRegistration> registrations = registrationRepository.findByContestId(contest.getId());
        List<ContestProblem> contestProblems = problemRepository.findByContestIdOrderByPositionAsc(contest.getId());
        int totalProblems = contestProblems.size();

        // Step 2: Apply marking scheme (abstract)
        Map<UUID, List<ContestSubmission>> byUser = allSubmissions.stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getId()));

        List<ParticipantScore> scores = new ArrayList<>();
        for (ContestRegistration reg : registrations) {
            UUID userId = reg.getUser().getId();
            List<ContestSubmission> userSubs = byUser.getOrDefault(userId, List.of());
            ParticipantScore ps = applyMarkingScheme(userId, userSubs, contestProblems, totalProblems);
            scores.add(ps);
        }

        // Step 3: Compute ranks (sorted by score DESC, then time ASC)
        scores.sort(Comparator
                .comparingInt(ParticipantScore::getTotalScore).reversed()
                .thenComparingInt(ParticipantScore::getTimeTakenSecs));

        // Step 4: Compute percentiles
        int totalParticipants = scores.size();
        List<ContestResult> results = new ArrayList<>();

        for (int i = 0; i < scores.size(); i++) {
            ParticipantScore ps = scores.get(i);
            int rank = i + 1;
            double percentile = totalParticipants > 1
                    ? ((double) (totalParticipants - rank) / (totalParticipants - 1)) * 100.0
                    : 100.0;

            results.add(ContestResult.builder()
                    .contest(contest)
                    .user(User.builder().id(ps.getUserId()).build())
                    .totalScore(ps.getTotalScore())
                    .rank(rank)
                    .percentile(Math.round(percentile * 100.0) / 100.0)
                    .correctCount(ps.getCorrectCount())
                    .wrongCount(ps.getWrongCount())
                    .unattemptedCount(ps.getUnattemptedCount())
                    .timeTakenSecs(ps.getTimeTakenSecs())
                    .subjectBreakdown(ps.getSubjectBreakdown())
                    .build());
        }

        log.info("Calculated {} results for contest={}", results.size(), contest.getId());
        return results;
    }

    /**
     * Abstract — contest-type-specific marking scheme application.
     */
    protected abstract ParticipantScore applyMarkingScheme(
            UUID userId,
            List<ContestSubmission> submissions,
            List<ContestProblem> contestProblems,
            int totalProblems);
}
