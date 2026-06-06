package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.constant.SprintConstants;
import com.preparex.preparex_backend.dto.request.SprintAnswerRequestDto;
import com.preparex.preparex_backend.dto.request.SprintStartRequestDto;
import com.preparex.preparex_backend.dto.response.*;
import com.preparex.preparex_backend.entity.Problem;
import com.preparex.preparex_backend.entity.SprintAnswer;
import com.preparex.preparex_backend.entity.SprintSession;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.SprintAnswerStatus;
import com.preparex.preparex_backend.enums.SprintSessionStatus;
import com.preparex.preparex_backend.exception.*;
import com.preparex.preparex_backend.mapper.ProblemMapper;
import com.preparex.preparex_backend.redis.SprintSessionState;
import com.preparex.preparex_backend.repository.ProblemRepository;
import com.preparex.preparex_backend.repository.SprintAnswerRepository;
import com.preparex.preparex_backend.repository.SprintSessionRepository;
import com.preparex.preparex_backend.repository.UserRepository;
import com.preparex.preparex_backend.service.ScoringService;
import com.preparex.preparex_backend.service.SprintLeaderboardService;
import com.preparex.preparex_backend.service.SprintService;
import com.preparex.preparex_backend.service.strategy.ScoringResult;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Sprint service implementation managing the full lifecycle of a 30-minute timed blitz session.
 *
 * <p>Session lifecycle:
 * <ol>
 *   <li>Start — RLock prevents duplicate sessions, generates question queue, saves to Redis + DB</li>
 *   <li>Answer — scores via ScoringService, calculates bonuses, persists SprintAnswer</li>
 *   <li>Skip — decrements skip counter, recycles question to back of queue</li>
 *   <li>End — finalizes DB entity, updates leaderboard ZSets, deletes Redis state</li>
 * </ol>
 * </p>
 *
 * <p>The 30-minute limit is server-enforced via startedAt timestamp in Redis.
 * On every answer/skip, elapsed time is checked and the session auto-ends if expired.</p>
 */
@Slf4j
@Service
public class SprintServiceImpl implements SprintService {

    private final SprintSessionRepository sprintSessionRepository;
    private final SprintAnswerRepository sprintAnswerRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final ScoringService scoringService;
    private final SprintLeaderboardService sprintLeaderboardService;
    private final ProblemMapper problemMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    public SprintServiceImpl(SprintSessionRepository sprintSessionRepository,
                             SprintAnswerRepository sprintAnswerRepository,
                             ProblemRepository problemRepository,
                             UserRepository userRepository,
                             ScoringService scoringService,
                             SprintLeaderboardService sprintLeaderboardService,
                             ProblemMapper problemMapper,
                             RedisTemplate<String, Object> redisTemplate,
                             RedissonClient redissonClient) {
        this.sprintSessionRepository = sprintSessionRepository;
        this.sprintAnswerRepository = sprintAnswerRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.scoringService = scoringService;
        this.sprintLeaderboardService = sprintLeaderboardService;
        this.problemMapper = problemMapper;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    @Override
    @Transactional
    public SprintStartResponseDto startSprint(UUID userId, SprintStartRequestDto request) {
        String lockKey = RedisKeyConstants.sprintUserLockKey(userId.toString());
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new SprintAlreadyActiveException("Could not acquire lock to start sprint session");
            }

            // Check for existing active session
            sprintSessionRepository.findByUserIdAndStatus(userId, SprintSessionStatus.ACTIVE)
                    .ifPresent(existing -> {
                        throw new SprintAlreadyActiveException(
                                "An active sprint session already exists: " + existing.getId());
                    });

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Generate question queue filtered by subject/difficulty/exam
            List<UUID> questionQueue = generateQuestionQueue(request, userId);

            if (questionQueue.isEmpty()) {
                throw new SprintSessionNotFoundException(
                        "No problems available for the selected filters");
            }

            Instant startedAt = Instant.now();
            Instant expiresAt = startedAt.plus(Duration.ofMinutes(SprintConstants.SESSION_DURATION_MINS));

            // Persist sprint session entity
            SprintSession session = SprintSession.builder()
                    .user(user)
                    .status(SprintSessionStatus.ACTIVE)
                    .subjectFilter(request.getSubjectFilter())
                    .difficultyFilter(request.getDifficultyFilter())
                    .examId(request.getExamId())
                    .startedAt(startedAt)
                    .build();
            session = sprintSessionRepository.save(session);

            // Save session state to Redis for fast access during sprint
            SprintSessionState state = SprintSessionState.builder()
                    .sessionId(session.getId())
                    .userId(userId)
                    .status(SprintSessionStatus.ACTIVE.name())
                    .subjectFilter(request.getSubjectFilter())
                    .difficultyFilter(request.getDifficultyFilter())
                    .questionQueue(questionQueue)
                    .currentIndex(0)
                    .skipsRemaining(SprintConstants.MAX_SKIPS)
                    .skippedQueue(new ArrayList<>())
                    .startedAt(startedAt)
                    .sprintPoints(0)
                    .build();

            String sessionKey = RedisKeyConstants.sprintSessionKey(session.getId().toString());
            redisTemplate.opsForValue().set(sessionKey, state,
                    SprintConstants.SESSION_REDIS_TTL_MINS, TimeUnit.MINUTES);

            // Load first question
            ProblemDetailResponseDto firstQuestion = loadProblemDto(questionQueue.get(0));

            log.info("Sprint session started: sessionId={}, userId={}, queueSize={}",
                    session.getId(), userId, questionQueue.size());

            return SprintStartResponseDto.builder()
                    .sessionId(session.getId())
                    .firstQuestion(firstQuestion)
                    .totalQuestions(questionQueue.size())
                    .skipsRemaining(SprintConstants.MAX_SKIPS)
                    .expiresAt(expiresAt)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SprintAlreadyActiveException("Sprint start interrupted");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public SprintAnswerResponseDto answerQuestion(UUID userId, UUID sessionId,
                                                   SprintAnswerRequestDto request) {
        SprintSessionState state = loadAndValidateSession(userId, sessionId);

        // Check time — auto-end if expired
        long timeRemaining = calculateTimeRemaining(state);
        if (timeRemaining <= 0) {
            SprintSummaryDto summary = endSprintInternal(userId, sessionId, state);
            return SprintAnswerResponseDto.builder()
                    .correct(null)
                    .pointsAwarded(0)
                    .nextQuestion(null)
                    .timeRemainingSecs(0)
                    .sessionStats(buildSessionStats(state))
                    .sessionEnded(true)
                    .build();
        }

        // Load problem and score
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ProblemNotFoundException(
                        "Problem not found: " + request.getProblemId()));

        ScoringResult result = scoringService.score(
                problem.getQuestionType(),
                request.getAnswer(),
                problem.getAnswerKey());

        // Calculate sprint points with bonuses
        int basePoints = calculateBasePoints(problem.getDifficulty(), result.isCorrect());
        int timeBonus = calculateTimeBonus(request.getTimeTakenSecs());
        int firstAttemptBonus = isFirstAttempt(state, request.getProblemId())
                ? SprintConstants.FIRST_ATTEMPT_BONUS : 0;
        int totalPoints = basePoints + (result.isCorrect() ? timeBonus + firstAttemptBonus : 0);

        // Persist sprint answer
        SprintAnswerStatus answerStatus = result.isCorrect()
                ? SprintAnswerStatus.CORRECT : SprintAnswerStatus.WRONG;

        SprintAnswer sprintAnswer = SprintAnswer.builder()
                .session(sprintSessionRepository.getReferenceById(sessionId))
                .problem(problem)
                .status(answerStatus)
                .marksAwarded(totalPoints)
                .timeTakenSecs(request.getTimeTakenSecs())
                .build();
        sprintAnswerRepository.save(sprintAnswer);

        // Update Redis state
        state.setSprintPoints(state.getSprintPoints() + totalPoints);
        state.setAttempted(state.getAttempted() + 1);
        if (result.isCorrect()) {
            state.setCorrect(state.getCorrect() + 1);
        } else {
            state.setWrong(state.getWrong() + 1);
        }

        // Advance to next question
        ProblemDetailResponseDto nextQuestion = advanceToNextQuestion(state);
        boolean sessionEnded = false;

        // Check if no more questions
        if (nextQuestion == null) {
            endSprintInternal(userId, sessionId, state);
            sessionEnded = true;
        } else {
            saveStateToRedis(state);
        }

        log.debug("Sprint answer: sessionId={}, problemId={}, correct={}, points={}",
                sessionId, request.getProblemId(), result.isCorrect(), totalPoints);

        return SprintAnswerResponseDto.builder()
                .correct(result.isCorrect())
                .pointsAwarded(totalPoints)
                .nextQuestion(nextQuestion)
                .timeRemainingSecs(Math.max(0, calculateTimeRemaining(state)))
                .sessionStats(buildSessionStats(state))
                .sessionEnded(sessionEnded)
                .build();
    }

    @Override
    @Transactional
    public SprintAnswerResponseDto skipQuestion(UUID userId, UUID sessionId) {
        SprintSessionState state = loadAndValidateSession(userId, sessionId);

        // Check time — auto-end if expired
        long timeRemaining = calculateTimeRemaining(state);
        if (timeRemaining <= 0) {
            endSprintInternal(userId, sessionId, state);
            return SprintAnswerResponseDto.builder()
                    .correct(null)
                    .pointsAwarded(0)
                    .nextQuestion(null)
                    .timeRemainingSecs(0)
                    .sessionStats(buildSessionStats(state))
                    .sessionEnded(true)
                    .build();
        }

        if (state.getSkipsRemaining() <= 0) {
            throw new SprintNoSkipsException("No skips remaining. You have used all 5 skips.");
        }

        // Get current question and add to skipped queue for recycling
        UUID currentProblemId = getCurrentProblemId(state);
        if (currentProblemId != null) {
            state.getSkippedQueue().add(currentProblemId);

            // Persist skip as SprintAnswer
            SprintAnswer skipAnswer = SprintAnswer.builder()
                    .session(sprintSessionRepository.getReferenceById(sessionId))
                    .problem(problemRepository.getReferenceById(currentProblemId))
                    .status(SprintAnswerStatus.SKIPPED)
                    .marksAwarded(0)
                    .build();
            sprintAnswerRepository.save(skipAnswer);
        }

        state.setSkipsRemaining(state.getSkipsRemaining() - 1);
        state.setSkipped(state.getSkipped() + 1);

        // Advance to next question
        ProblemDetailResponseDto nextQuestion = advanceToNextQuestion(state);
        boolean sessionEnded = false;

        if (nextQuestion == null) {
            endSprintInternal(userId, sessionId, state);
            sessionEnded = true;
        } else {
            saveStateToRedis(state);
        }

        log.debug("Sprint skip: sessionId={}, skipsRemaining={}", sessionId, state.getSkipsRemaining());

        return SprintAnswerResponseDto.builder()
                .correct(null)
                .pointsAwarded(0)
                .nextQuestion(nextQuestion)
                .timeRemainingSecs(Math.max(0, calculateTimeRemaining(state)))
                .sessionStats(buildSessionStats(state))
                .sessionEnded(sessionEnded)
                .build();
    }

    @Override
    @Transactional
    public SprintSummaryDto endSprint(UUID userId, UUID sessionId) {
        SprintSessionState state = loadAndValidateSession(userId, sessionId);
        return endSprintInternal(userId, sessionId, state);
    }

    @Override
    public SprintStatusResponseDto getStatus(UUID userId, UUID sessionId) {
        SprintSessionState state = loadAndValidateSession(userId, sessionId);

        long timeRemaining = calculateTimeRemaining(state);
        ProblemDetailResponseDto currentQuestion = null;

        UUID currentProblemId = getCurrentProblemId(state);
        if (currentProblemId != null && timeRemaining > 0) {
            currentQuestion = loadProblemDto(currentProblemId);
        }

        return SprintStatusResponseDto.builder()
                .sessionId(sessionId)
                .status(state.getStatus())
                .timeRemainingSecs(Math.max(0, timeRemaining))
                .currentQuestionIndex(state.getCurrentIndex())
                .totalQuestionsInQueue(state.getQuestionQueue().size())
                .currentQuestion(currentQuestion)
                .sessionStats(buildSessionStats(state))
                .build();
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    /**
     * Generates a queue of problem IDs, filtered by user preferences and
     * bounded by the user's historical accuracy (simple ELO-like selection).
     */
    private List<UUID> generateQuestionQueue(SprintStartRequestDto request, UUID userId) {
        Difficulty difficultyFilter = null;
        if (request.getDifficultyFilter() != null && !request.getDifficultyFilter().isBlank()) {
            try {
                difficultyFilter = Difficulty.valueOf(request.getDifficultyFilter().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid difficulty filter: {}", request.getDifficultyFilter());
            }
        }

        // Resolve subject filter to subject ID
        Integer subjectId = null;
        if (request.getSubjectFilter() != null && !request.getSubjectFilter().isBlank()) {
            try {
                subjectId = Integer.parseInt(request.getSubjectFilter());
            } catch (NumberFormatException e) {
                log.warn("Subject filter is not a numeric ID: {}", request.getSubjectFilter());
            }
        }

        // Query problems with filters, ordered randomly
        // Using pageable with a large size and JPQL random ordering
        var page = problemRepository.findByFilters(
                subjectId,
                null, // topicId — not filtered in sprint
                difficultyFilter,
                null, // questionType — all types
                request.getExamId(),
                false, // pyqOnly
                PageRequest.of(0, SprintConstants.QUESTION_QUEUE_SIZE)
        );

        List<UUID> ids = new ArrayList<>(page.getContent().stream()
                .map(Problem::getId)
                .toList());

        // Shuffle for randomization (DB ordering is deterministic by default)
        Collections.shuffle(ids);

        log.info("Generated sprint question queue: {} problems for userId={}",
                ids.size(), userId);

        return ids;
    }

    /**
     * Loads a SprintSessionState from Redis and validates ownership.
     *
     * @throws SprintSessionNotFoundException if session not found in Redis
     * @throws SprintSessionExpiredException  if session has expired
     */
    private SprintSessionState loadAndValidateSession(UUID userId, UUID sessionId) {
        String sessionKey = RedisKeyConstants.sprintSessionKey(sessionId.toString());
        Object raw = redisTemplate.opsForValue().get(sessionKey);

        if (raw == null) {
            throw new SprintSessionNotFoundException(
                    "Sprint session not found or expired: " + sessionId);
        }

        SprintSessionState state;
        if (raw instanceof SprintSessionState) {
            state = (SprintSessionState) raw;
        } else {
            throw new SprintSessionNotFoundException(
                    "Sprint session data corrupted: " + sessionId);
        }

        if (!userId.equals(state.getUserId())) {
            throw new SprintSessionNotFoundException(
                    "Sprint session does not belong to current user");
        }

        return state;
    }

    /**
     * Calculates remaining time in seconds from the session start.
     */
    private long calculateTimeRemaining(SprintSessionState state) {
        Instant expiresAt = state.getStartedAt()
                .plus(Duration.ofMinutes(SprintConstants.SESSION_DURATION_MINS));
        return Duration.between(Instant.now(), expiresAt).getSeconds();
    }

    /**
     * Calculates base points based on problem difficulty.
     */
    private int calculateBasePoints(Difficulty difficulty, boolean correct) {
        if (!correct) {
            return SprintConstants.WRONG;
        }
        return switch (difficulty) {
            case EASY -> SprintConstants.EASY_CORRECT;
            case MEDIUM -> SprintConstants.MEDIUM_CORRECT;
            case HARD -> SprintConstants.HARD_CORRECT;
        };
    }

    /**
     * Calculates time bonus points based on answer speed.
     */
    private int calculateTimeBonus(Integer timeTakenSecs) {
        if (timeTakenSecs == null) {
            return 0;
        }
        if (timeTakenSecs < SprintConstants.TIME_BONUS_FAST_THRESHOLD_SECS) {
            return SprintConstants.TIME_BONUS_FAST;
        }
        if (timeTakenSecs < SprintConstants.TIME_BONUS_MODERATE_THRESHOLD_SECS) {
            return SprintConstants.TIME_BONUS_MODERATE;
        }
        return 0;
    }

    /**
     * Checks if this is the user's first attempt at the problem
     * (not a recycled skip). First attempt earns a bonus.
     */
    private boolean isFirstAttempt(SprintSessionState state, UUID problemId) {
        return !state.getSkippedQueue().contains(problemId);
    }

    /**
     * Gets the problem ID at the current queue position.
     * Falls back to the skipped queue if the main queue is exhausted.
     */
    private UUID getCurrentProblemId(SprintSessionState state) {
        if (state.getCurrentIndex() < state.getQuestionQueue().size()) {
            return state.getQuestionQueue().get(state.getCurrentIndex());
        }
        // Try skipped queue
        if (!state.getSkippedQueue().isEmpty()) {
            return state.getSkippedQueue().get(0);
        }
        return null;
    }

    /**
     * Advances to the next question in the queue.
     * After the main queue is exhausted, cycles through skipped questions.
     *
     * @return the next question DTO, or null if no more questions
     */
    private ProblemDetailResponseDto advanceToNextQuestion(SprintSessionState state) {
        state.setCurrentIndex(state.getCurrentIndex() + 1);

        // Try main queue first
        if (state.getCurrentIndex() < state.getQuestionQueue().size()) {
            UUID nextId = state.getQuestionQueue().get(state.getCurrentIndex());
            return loadProblemDto(nextId);
        }

        // Try skipped queue (recycle skipped questions)
        if (!state.getSkippedQueue().isEmpty()) {
            UUID skippedId = state.getSkippedQueue().remove(0);
            return loadProblemDto(skippedId);
        }

        // No more questions
        return null;
    }

    /**
     * Loads a problem entity and converts to detail DTO (without answer_key).
     */
    private ProblemDetailResponseDto loadProblemDto(UUID problemId) {
        return problemRepository.findByIdAndIsActiveTrue(problemId)
                .map(problemMapper::toDetailDto)
                .orElse(null);
    }

    /**
     * Saves the current session state back to Redis with the remaining TTL.
     */
    private void saveStateToRedis(SprintSessionState state) {
        String sessionKey = RedisKeyConstants.sprintSessionKey(state.getSessionId().toString());
        long timeRemaining = calculateTimeRemaining(state);
        // Add 5-minute buffer to TTL
        long ttlSeconds = Math.max(timeRemaining + 300, 60);
        redisTemplate.opsForValue().set(sessionKey, state, ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * Finalizes a sprint session: updates DB, updates leaderboard, deletes Redis state.
     */
    private SprintSummaryDto endSprintInternal(UUID userId, UUID sessionId,
                                                SprintSessionState state) {
        Instant endedAt = Instant.now();
        long durationSecs = Duration.between(state.getStartedAt(), endedAt).getSeconds();

        // Update DB entity
        SprintSession session = sprintSessionRepository.findById(sessionId)
                .orElseThrow(() -> new SprintSessionNotFoundException(
                        "Sprint session not found in database: " + sessionId));

        session.setStatus(SprintSessionStatus.COMPLETED);
        session.setEndedAt(endedAt);
        session.setTotalQuestionsAttempted(state.getAttempted());
        session.setTotalCorrect(state.getCorrect());
        session.setTotalWrong(state.getWrong());
        session.setTotalSkipped(state.getSkipped());
        session.setSprintPoints(state.getSprintPoints());
        sprintSessionRepository.save(session);

        // Update weekly/monthly leaderboard
        if (state.getSprintPoints() > 0) {
            sprintLeaderboardService.addPoints(userId, state.getSprintPoints());
        }

        // Delete Redis session state
        String sessionKey = RedisKeyConstants.sprintSessionKey(sessionId.toString());
        redisTemplate.delete(sessionKey);

        log.info("Sprint session ended: sessionId={}, userId={}, points={}, duration={}s",
                sessionId, userId, state.getSprintPoints(), durationSecs);

        return SprintSummaryDto.builder()
                .sessionId(sessionId)
                .totalAttempted(state.getAttempted())
                .totalCorrect(state.getCorrect())
                .totalWrong(state.getWrong())
                .totalSkipped(state.getSkipped())
                .sprintPoints(state.getSprintPoints())
                .durationSecs(durationSecs)
                .build();
    }

    /**
     * Builds running session stats DTO from the current Redis state.
     */
    private SprintSessionStatsDto buildSessionStats(SprintSessionState state) {
        return SprintSessionStatsDto.builder()
                .attempted(state.getAttempted())
                .correct(state.getCorrect())
                .wrong(state.getWrong())
                .skipped(state.getSkipped())
                .points(state.getSprintPoints())
                .skipsRemaining(state.getSkipsRemaining())
                .build();
    }
}
