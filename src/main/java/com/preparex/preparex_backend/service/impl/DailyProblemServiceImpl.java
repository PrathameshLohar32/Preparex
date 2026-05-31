package com.preparex.preparex_backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.dto.request.SubmitRequestDto;
import com.preparex.preparex_backend.dto.response.DailyCompletionResponseDto;
import com.preparex.preparex_backend.dto.response.DailyProblemResponseDto;
import com.preparex.preparex_backend.dto.response.SubmissionResponseDto;
import com.preparex.preparex_backend.dto.response.UserStreakResponseDto;
import com.preparex.preparex_backend.entity.*;
import com.preparex.preparex_backend.enums.SubmissionSource;
import com.preparex.preparex_backend.exception.ProblemNotFoundException;
import com.preparex.preparex_backend.repository.DailyCompletionRepository;
import com.preparex.preparex_backend.repository.DailyProblemRepository;
import com.preparex.preparex_backend.repository.UserStreakRepository;
import com.preparex.preparex_backend.service.DailyProblemService;
import com.preparex.preparex_backend.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Service implementation for daily challenge operations.
 *
 * <p><strong>Caching strategy:</strong></p>
 * <ul>
 *   <li>"daily:today" — cached until midnight (TTL = seconds until midnight)</li>
 *   <li>"streak:{userId}" — cached for 30 minutes</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyProblemServiceImpl implements DailyProblemService {

    private final DailyProblemRepository dailyProblemRepository;
    private final DailyCompletionRepository dailyCompletionRepository;
    private final UserStreakRepository userStreakRepository;
    private final SubmissionService submissionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @SuppressWarnings("unchecked")
    public List<DailyProblemResponseDto> getToday(UUID userId) {
        log.info("Fetching today's daily problems for userId={}", userId);

        LocalDate today = LocalDate.now();

        // Try Redis cache first
        String cacheKey = RedisKeyConstants.DAILY_TODAY_KEY;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache HIT for daily:today");
                String json = objectMapper.writeValueAsString(cached);
                List<DailyProblemResponseDto> cachedProblems = objectMapper.readValue(json,
                        new TypeReference<List<DailyProblemResponseDto>>() {});
                // Enrich with user-specific completion status
                return enrichWithCompletionStatus(cachedProblems, userId);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached daily problems, fetching from DB", e);
        }

        log.debug("Cache MISS for daily:today");

        List<DailyProblem> dailyProblems = dailyProblemRepository
                .findByScheduledDateAndIsActiveTrue(today);

        List<DailyProblemResponseDto> dtos = dailyProblems.stream()
                .map(dp -> toDailyProblemDto(dp, false))
                .toList();

        // Cache until midnight
        try {
            long ttlSeconds = Duration.between(
                    LocalDateTime.now(),
                    LocalDateTime.of(today.plusDays(1), LocalTime.MIDNIGHT)
            ).getSeconds();

            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(cacheKey, dtos, ttlSeconds, TimeUnit.SECONDS);
                log.debug("Cached daily:today with TTL={}s", ttlSeconds);
            }
        } catch (Exception e) {
            log.warn("Failed to cache daily:today", e);
        }

        return enrichWithCompletionStatus(dtos, userId);
    }

    @Override
    @Transactional
    public DailyCompletionResponseDto complete(UUID userId, Integer dailyProblemId, SubmitRequestDto request) {
        log.info("Completing daily problem: userId={}, dailyProblemId={}", userId, dailyProblemId);

        // Check for idempotent completion
        Optional<DailyCompletion> existing = dailyCompletionRepository
                .findByUserIdAndDailyProblemId(userId, dailyProblemId);

        if (existing.isPresent()) {
            log.info("Daily problem already completed by userId={}, returning existing result", userId);
            return DailyCompletionResponseDto.builder()
                    .correct(existing.get().getSubmission() != null
                            && existing.get().getSubmission().getStatus() == com.preparex.preparex_backend.enums.SubmissionStatus.CORRECT)
                    .status(existing.get().getSubmission() != null
                            ? existing.get().getSubmission().getStatus() : null)
                    .marksAwarded(existing.get().getSubmission() != null
                            ? existing.get().getSubmission().getMarksAwarded() : 0)
                    .explanation("Already completed")
                    .alreadyCompleted(true)
                    .build();
        }

        // Get the daily problem
        DailyProblem dailyProblem = dailyProblemRepository.findById(dailyProblemId)
                .orElseThrow(() -> new ProblemNotFoundException("Daily problem not found: " + dailyProblemId));

        // Override source to DAILY for scoring + streak tracking
        request.setProblemId(dailyProblem.getProblem().getId());
        request.setSource(SubmissionSource.DAILY);

        // Submit via SubmissionService (scores + publishes event)
        SubmissionResponseDto submissionResult = submissionService.submit(userId, request);

        // Create daily completion record
        DailyCompletion completion = DailyCompletion.builder()
                .user(User.builder().id(userId).build())
                .dailyProblem(dailyProblem)
                .completedDate(LocalDate.now())
                .build();

        dailyCompletionRepository.save(completion);

        log.info("Daily problem {} completed by userId={}, correct={}",
                dailyProblemId, userId, submissionResult.getCorrect());

        return DailyCompletionResponseDto.builder()
                .correct(submissionResult.getCorrect())
                .status(submissionResult.getStatus())
                .marksAwarded(submissionResult.getMarksAwarded())
                .explanation(submissionResult.getExplanation())
                .alreadyCompleted(false)
                .build();
    }

    @Override
    public Map<LocalDate, String> getCalendar(UUID userId) {
        log.info("Fetching 90-day calendar for userId={}", userId);

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(89);

        // Get all completion dates in range
        List<DailyCompletion> completions = dailyCompletionRepository
                .findByUserIdAndCompletedDateBetween(userId, start, today);

        Set<LocalDate> completedDates = new HashSet<>();
        completions.forEach(c -> completedDates.add(c.getCompletedDate()));

        // Get all scheduled dates in range
        List<DailyProblem> scheduled = dailyProblemRepository
                .findByScheduledDateAndIsActiveTrue(today);

        // Build calendar
        Map<LocalDate, String> calendar = new LinkedHashMap<>();
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            if (date.isAfter(today)) {
                calendar.put(date, "FUTURE");
            } else if (completedDates.contains(date)) {
                calendar.put(date, "SOLVED");
            } else {
                calendar.put(date, "MISSED");
            }
        }

        return calendar;
    }

    @Override
    public UserStreakResponseDto getStreak(UUID userId) {
        log.info("Fetching streak for userId={}", userId);

        // Check Redis cache
        String cacheKey = RedisKeyConstants.streakCacheKey(userId.toString());
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache HIT for streak userId={}", userId);
                String json = objectMapper.writeValueAsString(cached);
                return objectMapper.readValue(json, UserStreakResponseDto.class);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached streak for userId={}", userId, e);
        }

        log.debug("Cache MISS for streak userId={}", userId);

        UserStreak streak = userStreakRepository.findByUserId(userId)
                .orElse(UserStreak.builder()
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());

        UserStreakResponseDto dto = UserStreakResponseDto.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .lastActiveDate(streak.getLastActiveDate())
                .build();

        // Cache for 30 minutes
        try {
            redisTemplate.opsForValue().set(cacheKey, dto,
                    RedisKeyConstants.STREAK_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache streak for userId={}", userId, e);
        }

        return dto;
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    private DailyProblemResponseDto toDailyProblemDto(DailyProblem dp, boolean isCompleted) {
        Problem problem = dp.getProblem();
        return DailyProblemResponseDto.builder()
                .dailyProblemId(dp.getId())
                .problemId(problem.getId())
                .title(problem.getTitle())
                .slug(problem.getSlug())
                .subjectName(dp.getSubject().getName())
                .difficulty(problem.getDifficulty())
                .questionType(problem.getQuestionType())
                .isCompletedByUser(isCompleted)
                .build();
    }

    private List<DailyProblemResponseDto> enrichWithCompletionStatus(
            List<DailyProblemResponseDto> dtos, UUID userId) {
        return dtos.stream()
                .map(dto -> {
                    boolean completed = dailyCompletionRepository
                            .existsByUserIdAndDailyProblemId(userId, dto.getDailyProblemId());
                    dto.setIsCompletedByUser(completed);
                    return dto;
                })
                .toList();
    }
}
