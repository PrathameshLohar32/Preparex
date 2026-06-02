package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.dto.request.AddContestProblemRequestDto;
import com.preparex.preparex_backend.dto.request.ContestSubmitRequestDto;
import com.preparex.preparex_backend.dto.request.CreateContestRequestDto;
import com.preparex.preparex_backend.dto.response.ContestResponseDto;
import com.preparex.preparex_backend.dto.response.ContestResultResponseDto;
import com.preparex.preparex_backend.dto.response.LeaderboardEntryDto;
import com.preparex.preparex_backend.entity.*;
import com.preparex.preparex_backend.enums.ContestStatus;
import com.preparex.preparex_backend.event.ContestSubmissionEvent;
import com.preparex.preparex_backend.exception.ContestException;
import com.preparex.preparex_backend.exception.ContestSubmissionException;
import com.preparex.preparex_backend.exception.ProblemNotFoundException;
import com.preparex.preparex_backend.repository.*;
import com.preparex.preparex_backend.service.ContestService;
import com.preparex.preparex_backend.service.contest.ContestKafkaProducer;
import com.preparex.preparex_backend.service.contest.LeaderboardService;
import com.preparex.preparex_backend.service.contest.chain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Core contest service implementation.
 * Handles student flows (list, register, submit, leaderboard, results)
 * and admin flows (create, publish, add problems, force end).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestServiceImpl implements ContestService {

    private final ContestRepository contestRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ContestRegistrationRepository registrationRepository;
    private final ContestSubmissionRepository contestSubmissionRepository;
    private final ContestResultRepository contestResultRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final ContestKafkaProducer kafkaProducer;
    private final LeaderboardService leaderboardService;
    private final RedissonClient redissonClient;

    // ── Student APIs ────────────────────────────────────────────────────

    @Override
    public Page<ContestResponseDto> listContests(ContestStatus status, int page, int size, UUID userId) {
        log.info("Listing contests: status={}, page={}, size={}", status, page, size);

        Page<Contest> contests;
        if (status != null) {
            contests = contestRepository.findByStatusOrderByStartsAtDesc(
                    status, PageRequest.of(page, Math.min(size, 50)));
        } else {
            contests = contestRepository.findByStatusInOrderByStartsAtDesc(
                    List.of(ContestStatus.SCHEDULED, ContestStatus.LIVE, ContestStatus.ENDED,
                            ContestStatus.RESULTS_PUBLISHED),
                    PageRequest.of(page, Math.min(size, 50)));
        }

        return contests.map(c -> toResponseDto(c, userId));
    }

    @Override
    public ContestResponseDto getContest(UUID contestId, UUID userId) {
        Contest contest = findContestOrThrow(contestId);
        return toResponseDto(contest, userId);
    }

    @Override
    @Transactional
    public void register(UUID contestId, UUID userId) {
        log.info("Registering user={} for contest={}", userId, contestId);

        RLock lock = redissonClient.getLock("contest:register:" + contestId + ":" + userId);
        try {
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                throw new ContestException("Registration is being processed, please try again");
            }

            try {
                // Idempotent — if already registered, return silently
                if (registrationRepository.existsByContestIdAndUserId(contestId, userId)) {
                    log.info("User {} already registered for contest {}", userId, contestId);
                    return;
                }

                Contest contest = findContestOrThrow(contestId);

                if (contest.getStatus() != ContestStatus.SCHEDULED
                        && contest.getStatus() != ContestStatus.LIVE) {
                    throw new ContestException("Cannot register — contest is " + contest.getStatus());
                }

                // Check max participants
                if (contest.getMaxParticipants() != null) {
                    long count = registrationRepository.countByContestId(contestId);
                    if (count >= contest.getMaxParticipants()) {
                        throw new ContestException("Contest is full");
                    }
                }

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

                ContestRegistration registration = ContestRegistration.builder()
                        .contest(contest)
                        .user(user)
                        .build();

                registrationRepository.save(registration);
                log.info("User {} registered for contest {}", userId, contestId);

            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ContestException("Registration interrupted");
        }
    }

    @Override
    public List<Object> getQuestions(UUID contestId, UUID userId) {
        Contest contest = findContestOrThrow(contestId);

        if (contest.getStatus() != ContestStatus.LIVE) {
            throw new ContestException("Questions are only available when contest is LIVE");
        }

        if (!registrationRepository.existsByContestIdAndUserId(contestId, userId)) {
            throw new ContestException("You must be registered to view questions");
        }

        List<ContestProblem> cps = contestProblemRepository.findByContestIdOrderByPositionAsc(contestId);

        // Return problems WITHOUT answer_key
        return cps.stream()
                .map(cp -> {
                    Problem p = cp.getProblem();
                    Map<String, Object> question = new LinkedHashMap<>();
                    question.put("contestProblemId", cp.getId());
                    question.put("problemId", p.getId());
                    question.put("position", cp.getPosition());
                    question.put("title", p.getTitle());
                    question.put("questionType", p.getQuestionType());
                    question.put("difficulty", p.getDifficulty());
                    question.put("bodyText", p.getBodyText());
                    question.put("options", p.getOptions());
                    question.put("marks", cp.getMarks());
                    question.put("negativeMarks", cp.getNegativeMarks());
                    question.put("section", cp.getSection());
                    // answer_key is NEVER included
                    return (Object) question;
                })
                .toList();
    }

    @Override
    @Transactional
    public void submitAnswer(UUID contestId, UUID userId, ContestSubmitRequestDto request) {
        log.info("Contest submission: contest={}, user={}, problem={}",
                contestId, userId, request.getProblemId());

        // Run validation chain
        ContestSubmissionRequest chainReq = ContestSubmissionRequest.builder()
                .contestId(contestId)
                .userId(userId)
                .problemId(request.getProblemId())
                .answer(request.getAnswer())
                .timeTakenSecs(request.getTimeTakenSecs())
                .build();

        buildValidationChain().handle(chainReq);

        // Persist submission with PENDING-like state (status will be set by Kafka consumer)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Contest contest = findContestOrThrow(contestId);
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found"));

        ContestSubmission submission = ContestSubmission.builder()
                .contest(contest)
                .user(user)
                .problem(problem)
                .submittedAnswer(request.getAnswer())
                .timeTakenSecs(request.getTimeTakenSecs())
                .marksAwarded(0)
                .build();

        ContestSubmission saved = contestSubmissionRepository.save(submission);

        // Publish to Kafka for async scoring → 202 Accepted immediately
        ContestSubmissionEvent event = ContestSubmissionEvent.builder()
                .submissionId(saved.getId())
                .contestId(contestId)
                .userId(userId)
                .problemId(request.getProblemId())
                .answer(request.getAnswer())
                .timeTakenSecs(request.getTimeTakenSecs())
                .build();

        kafkaProducer.publishSubmission(event);
    }

    @Override
    @Transactional
    public void finalSubmit(UUID contestId, UUID userId) {
        log.info("Final submit: contest={}, user={}", contestId, userId);

        ContestRegistration reg = registrationRepository.findByContestIdAndUserId(contestId, userId)
                .orElseThrow(() -> new ContestException("Not registered for this contest"));

        if (reg.getFinalSubmittedAt() != null) {
            log.info("Already final submitted for contest={}, user={}", contestId, userId);
            return;
        }

        reg.setFinalSubmittedAt(Instant.now());
        registrationRepository.save(reg);
    }

    @Override
    public List<LeaderboardEntryDto> getLeaderboard(UUID contestId, UUID userId) {
        List<LeaderboardEntryDto> top50 = leaderboardService.getTop(contestId, 50);

        // Add caller's rank if not in top 50
        Long myRank = leaderboardService.getRank(contestId, userId);
        if (myRank != null && myRank > 50) {
            User user = userRepository.findById(userId).orElse(null);
            top50.add(LeaderboardEntryDto.builder()
                    .userId(userId)
                    .username(user != null ? user.getUsername() : "You")
                    .rank(myRank)
                    .score(0) // will be enriched from Redis
                    .build());
        }

        return top50;
    }

    @Override
    public ContestResultResponseDto getMyResult(UUID contestId, UUID userId) {
        Contest contest = findContestOrThrow(contestId);
        if (contest.getStatus() != ContestStatus.RESULTS_PUBLISHED) {
            throw new ContestException("Results not yet published");
        }

        ContestResult result = contestResultRepository.findByContestIdAndUserId(contestId, userId)
                .orElseThrow(() -> new ContestException("No result found — you may not have participated"));

        return toResultDto(result);
    }

    @Override
    public Page<ContestResultResponseDto> getResults(UUID contestId, int page, int size) {
        Contest contest = findContestOrThrow(contestId);
        if (contest.getStatus() != ContestStatus.RESULTS_PUBLISHED) {
            throw new ContestException("Results not yet published");
        }

        return contestResultRepository
                .findByContestIdOrderByRankAsc(contestId, PageRequest.of(page, Math.min(size, 100)))
                .map(this::toResultDto);
    }

    // ── Admin APIs ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public ContestResponseDto createContest(CreateContestRequestDto request) {
        log.info("Creating contest: title={}, type={}", request.getTitle(), request.getType());

        Contest contest = Contest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .examId(request.getExamId())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .durationMins(request.getDurationMins())
                .accessType(request.getAccessType())
                .paidAmountInr(request.getPaidAmountInr())
                .maxParticipants(request.getMaxParticipants())
                .build();

        if (request.getMarkingScheme() != null) {
            contest.setMarkingScheme(request.getMarkingScheme());
        }

        Contest saved = contestRepository.save(contest);
        log.info("Created contest id={}, status=DRAFT", saved.getId());

        return toResponseDto(saved, null);
    }

    @Override
    @Transactional
    public void publishContest(UUID contestId) {
        log.info("Publishing contest: id={}", contestId);

        Contest contest = findContestOrThrow(contestId);

        if (contest.getStatus() != ContestStatus.DRAFT) {
            throw new ContestException("Can only publish DRAFT contests. Current: " + contest.getStatus());
        }

        // Validate at least 1 problem
        long problemCount = contestProblemRepository.countByContestId(contestId);
        if (problemCount == 0) {
            throw new ContestException("Cannot publish — at least 1 problem must be attached");
        }

        // Validate start time in future
        if (contest.getStartsAt() == null || contest.getStartsAt().isBefore(Instant.now())) {
            throw new ContestException("Cannot publish — start time must be in the future");
        }

        contest.setStatus(ContestStatus.SCHEDULED);
        contest.setUpdatedAt(Instant.now());
        contestRepository.save(contest);

        log.info("Contest {} published: DRAFT → SCHEDULED", contestId);
    }

    @Override
    @Transactional
    public void addProblem(UUID contestId, AddContestProblemRequestDto request) {
        log.info("Adding problem {} to contest {}", request.getProblemId(), contestId);

        Contest contest = findContestOrThrow(contestId);

        if (contest.getStatus() != ContestStatus.DRAFT) {
            throw new ContestException("Can only add problems to DRAFT contests");
        }

        if (contestProblemRepository.existsByContestIdAndProblemId(contestId, request.getProblemId())) {
            throw new ContestException("Problem already attached to this contest");
        }

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found"));

        ContestProblem cp = ContestProblem.builder()
                .contest(contest)
                .problem(problem)
                .position(request.getPosition())
                .marks(request.getMarks())
                .negativeMarks(request.getNegativeMarks())
                .section(request.getSection())
                .build();

        contestProblemRepository.save(cp);
        log.info("Added problem {} to contest {} at position {}", problem.getId(), contestId, request.getPosition());
    }

    @Override
    @Transactional
    public void forceEndContest(UUID contestId) {
        log.info("Force ending contest: id={}", contestId);

        RLock lock = redissonClient.getLock("contest:state:" + contestId);
        try {
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                throw new ContestException("Could not acquire lock for contest state transition");
            }

            try {
                Contest contest = findContestOrThrow(contestId);

                if (contest.getStatus() != ContestStatus.LIVE) {
                    throw new ContestException("Can only force-end LIVE contests. Current: " + contest.getStatus());
                }

                contest.setStatus(ContestStatus.ENDED);
                contest.setUpdatedAt(Instant.now());
                contestRepository.save(contest);

                kafkaProducer.publishContestEnded(contestId);

                log.info("Contest {} force-ended: LIVE → ENDED", contestId);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ContestException("Force-end interrupted");
        }
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    private Contest findContestOrThrow(UUID contestId) {
        return contestRepository.findById(contestId)
                .orElseThrow(() -> new ContestException("Contest not found: " + contestId));
    }

    private ContestSubmissionHandler buildValidationChain() {
        TimeWindowHandler timeHandler = new TimeWindowHandler(contestRepository);
        RegistrationHandler regHandler = new RegistrationHandler(registrationRepository);
        DuplicateHandler dupHandler = new DuplicateHandler(contestSubmissionRepository);
        FormatHandler formatHandler = new FormatHandler();

        timeHandler.setNext(regHandler);
        regHandler.setNext(dupHandler);
        dupHandler.setNext(formatHandler);

        return timeHandler;
    }

    private ContestResponseDto toResponseDto(Contest c, UUID userId) {
        long regCount = registrationRepository.countByContestId(c.getId());
        long probCount = contestProblemRepository.countByContestId(c.getId());
        boolean isRegistered = userId != null
                && registrationRepository.existsByContestIdAndUserId(c.getId(), userId);

        return ContestResponseDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .type(c.getType())
                .status(c.getStatus())
                .examId(c.getExamId())
                .startsAt(c.getStartsAt())
                .endsAt(c.getEndsAt())
                .durationMins(c.getDurationMins())
                .markingScheme(c.getMarkingScheme())
                .accessType(c.getAccessType())
                .registeredCount(regCount)
                .problemCount(probCount)
                .isRegistered(isRegistered)
                .build();
    }

    private ContestResultResponseDto toResultDto(ContestResult r) {
        User user = r.getUser();
        String username = null;
        try {
            if (user != null && user.getId() != null) {
                username = userRepository.findById(user.getId())
                        .map(User::getUsername).orElse(null);
            }
        } catch (Exception ignored) {}

        return ContestResultResponseDto.builder()
                .contestId(r.getContest().getId())
                .userId(r.getUser().getId())
                .username(username)
                .totalScore(r.getTotalScore())
                .rank(r.getRank())
                .percentile(r.getPercentile())
                .correctCount(r.getCorrectCount())
                .wrongCount(r.getWrongCount())
                .unattemptedCount(r.getUnattemptedCount())
                .timeTakenSecs(r.getTimeTakenSecs())
                .subjectBreakdown(r.getSubjectBreakdown())
                .build();
    }
}
