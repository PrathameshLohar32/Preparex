package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.dto.request.SubmissionFilterRequestDto;
import com.preparex.preparex_backend.dto.request.SubmitRequestDto;
import com.preparex.preparex_backend.dto.response.SubmissionHistoryResponseDto;
import com.preparex.preparex_backend.dto.response.SubmissionResponseDto;
import com.preparex.preparex_backend.dto.response.SubjectStatsResponseDto;
import com.preparex.preparex_backend.entity.Problem;
import com.preparex.preparex_backend.entity.Submission;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import com.preparex.preparex_backend.event.SubmissionSavedEvent;
import com.preparex.preparex_backend.exception.ProblemNotFoundException;
import com.preparex.preparex_backend.repository.ProblemRepository;
import com.preparex.preparex_backend.repository.SubmissionRepository;
import com.preparex.preparex_backend.repository.UserRepository;
import com.preparex.preparex_backend.service.ScoringService;
import com.preparex.preparex_backend.service.SubmissionService;
import com.preparex.preparex_backend.service.strategy.ScoringResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Core implementation of SubmissionService.
 *
 * <p>Submit flow:</p>
 * <ol>
 *   <li>Load problem (answer_key from DB, never from cache)</li>
 *   <li>Resolve ScoringStrategy by QuestionType → score()</li>
 *   <li>Persist Submission entity</li>
 *   <li>Publish SubmissionSavedEvent (consumed async by streak + analytics listeners)</li>
 *   <li>Return scoring result (answer_key NEVER in response)</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionServiceImpl implements SubmissionService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final ScoringService scoringService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public SubmissionResponseDto submit(UUID userId, SubmitRequestDto request) {
        log.info("Processing submission: userId={}, problemId={}, source={}",
                userId, request.getProblemId(), request.getSource());

        // Load problem directly from DB (not cached) to get answer_key
        Problem problem = problemRepository.findByIdAndIsActiveTrue(request.getProblemId())
                .orElseThrow(() -> new ProblemNotFoundException(
                        "Problem not found with id: " + request.getProblemId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));

        // Score the submission
        ScoringResult result = scoringService.score(
                problem.getQuestionType(),
                request.getAnswer(),
                problem.getAnswerKey()
        );

        // Map scoring result to submission status
        SubmissionStatus status = result.isCorrect()
                ? SubmissionStatus.CORRECT
                : (result.getMarksAwarded() > 0 ? SubmissionStatus.PARTIAL : SubmissionStatus.WRONG);

        // Persist submission
        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .status(status)
                .submittedAnswer(request.getAnswer())
                .marksAwarded(result.getMarksAwarded())
                .timeTakenSecs(request.getTimeTakenSecs())
                .source(request.getSource())
                .build();

        Submission saved = submissionRepository.save(submission);
        log.info("Saved submission id={}, status={}, marks={}",
                saved.getId(), status, result.getMarksAwarded());

        // Publish event asynchronously for streak + analytics
        eventPublisher.publishEvent(new SubmissionSavedEvent(this, saved));

        return SubmissionResponseDto.builder()
                .correct(result.isCorrect())
                .status(status)
                .marksAwarded(result.getMarksAwarded())
                .explanation(result.getExplanation())
                .build();
    }

    @Override
    public Page<SubmissionHistoryResponseDto> getHistory(UUID userId, SubmissionFilterRequestDto filter) {
        log.info("Fetching submission history: userId={}, problemId={}, status={}, source={}",
                userId, filter.getProblemId(), filter.getStatus(), filter.getSource());

        int pageSize = Math.min(
                filter.getSize() != null ? filter.getSize() : DEFAULT_PAGE_SIZE,
                MAX_PAGE_SIZE
        );
        int pageNumber = filter.getPage() != null ? filter.getPage() : 0;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Submission> submissions = submissionRepository.findByFilters(
                userId,
                filter.getProblemId(),
                filter.getStatus(),
                filter.getSource(),
                pageable
        );

        return submissions.map(this::toHistoryDto);
    }

    @Override
    public SubjectStatsResponseDto getStats(UUID userId) {
        log.info("Fetching submission stats for userId={}", userId);

        List<Object[]> rawStats = submissionRepository.findSubjectStats(userId);

        // Build nested map: subjectName → difficulty → status → count
        Map<String, Map<String, SubjectStatsResponseDto.DifficultyCount>> statsMap = new LinkedHashMap<>();

        for (Object[] row : rawStats) {
            String subjectName = (String) row[0];
            String difficulty = row[1].toString();
            String status = row[2].toString();
            long count = (long) row[3];

            statsMap.computeIfAbsent(subjectName, k -> new LinkedHashMap<>());
            Map<String, SubjectStatsResponseDto.DifficultyCount> diffMap = statsMap.get(subjectName);

            SubjectStatsResponseDto.DifficultyCount dc = diffMap.computeIfAbsent(difficulty,
                    k -> SubjectStatsResponseDto.DifficultyCount.builder()
                            .correct(0L).wrong(0L).partial(0L).total(0L)
                            .build());

            switch (status) {
                case "CORRECT" -> dc.setCorrect(count);
                case "WRONG" -> dc.setWrong(count);
                case "PARTIAL" -> dc.setPartial(count);
            }
            dc.setTotal(dc.getCorrect() + dc.getWrong() + dc.getPartial());
        }

        List<SubjectStatsResponseDto.SubjectStat> subjects = statsMap.entrySet().stream()
                .map(entry -> SubjectStatsResponseDto.SubjectStat.builder()
                        .subjectName(entry.getKey())
                        .difficulties(entry.getValue())
                        .build())
                .toList();

        return SubjectStatsResponseDto.builder()
                .subjects(subjects)
                .build();
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    private SubmissionHistoryResponseDto toHistoryDto(Submission s) {
        return SubmissionHistoryResponseDto.builder()
                .id(s.getId())
                .problemId(s.getProblem().getId())
                .problemTitle(s.getProblem().getTitle())
                .problemSlug(s.getProblem().getSlug())
                .status(s.getStatus())
                .marksAwarded(s.getMarksAwarded())
                .timeTakenSecs(s.getTimeTakenSecs())
                .source(s.getSource())
                .submittedAt(s.getSubmittedAt())
                .build();
    }
}
