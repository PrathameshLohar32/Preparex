package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.SprintAnswerRequestDto;
import com.preparex.preparex_backend.dto.request.SprintStartRequestDto;
import com.preparex.preparex_backend.dto.response.*;
import com.preparex.preparex_backend.security.CustomUserDetails;
import com.preparex.preparex_backend.service.SprintLeaderboardService;
import com.preparex.preparex_backend.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Sprint mode controller — manages 30-minute timed blitz sessions.
 * All endpoints require authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sprint")
@RequiredArgsConstructor
@Tag(name = "Sprint", description = "Sprint mode APIs — start, answer, skip, end, leaderboard")
public class SprintController {

    private final SprintService sprintService;
    private final SprintLeaderboardService sprintLeaderboardService;

    @Operation(
            summary = "Start sprint session",
            description = "Creates a new 30-minute timed sprint. Returns sessionId and first question. "
                    + "Fails with 409 if user already has an active sprint."
    )
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<SprintStartResponseDto>> startSprint(
            @RequestBody(required = false) SprintStartRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SprintStartRequestDto req = request != null ? request : new SprintStartRequestDto();
        SprintStartResponseDto result = sprintService.startSprint(userDetails.getUserId(), req);

        log.info("Sprint started: sessionId={}, userId={}", result.getSessionId(), userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Sprint session started", result));
    }

    @Operation(
            summary = "Answer current question",
            description = "Scores the answer, awards points with bonuses, and returns the next question. "
                    + "Auto-ends the session if 30 minutes have elapsed."
    )
    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<ApiResponse<SprintAnswerResponseDto>> answerQuestion(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SprintAnswerRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SprintAnswerResponseDto result = sprintService.answerQuestion(
                userDetails.getUserId(), sessionId, request);
        return ResponseEntity.ok(ApiResponse.success("Answer processed", result));
    }

    @Operation(
            summary = "Skip current question",
            description = "Skips the current question (max 5 skips per session). "
                    + "Skipped questions are recycled to the back of the queue. "
                    + "Fails with 422 if no skips remaining."
    )
    @PostMapping("/{sessionId}/skip")
    public ResponseEntity<ApiResponse<SprintAnswerResponseDto>> skipQuestion(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SprintAnswerResponseDto result = sprintService.skipQuestion(
                userDetails.getUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Question skipped", result));
    }

    @Operation(
            summary = "End sprint session",
            description = "Ends the sprint early and returns the full summary. "
                    + "Updates the weekly/monthly leaderboard with earned points."
    )
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ApiResponse<SprintSummaryDto>> endSprint(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SprintSummaryDto result = sprintService.endSprint(userDetails.getUserId(), sessionId);
        log.info("Sprint ended: sessionId={}, points={}", sessionId, result.getSprintPoints());
        return ResponseEntity.ok(ApiResponse.success("Sprint session ended", result));
    }

    @Operation(
            summary = "Get sprint session status",
            description = "Returns current session state, time remaining, current question, and running stats."
    )
    @GetMapping("/{sessionId}/status")
    public ResponseEntity<ApiResponse<SprintStatusResponseDto>> getStatus(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SprintStatusResponseDto result = sprintService.getStatus(
                userDetails.getUserId(), sessionId);
        return ResponseEntity.ok(ApiResponse.success("Sprint status fetched", result));
    }

    @Operation(
            summary = "Get weekly sprint leaderboard",
            description = "Returns the top sprint performers for the current week."
    )
    @GetMapping("/leaderboard/weekly")
    public ResponseEntity<ApiResponse<List<SprintLeaderboardEntryDto>>> getWeeklyLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {

        List<SprintLeaderboardEntryDto> result = sprintLeaderboardService.getWeeklyTop(
                Math.min(limit, 100));
        return ResponseEntity.ok(ApiResponse.success("Weekly leaderboard fetched", result));
    }

    @Operation(
            summary = "Get monthly sprint leaderboard",
            description = "Returns the top sprint performers for the current month."
    )
    @GetMapping("/leaderboard/monthly")
    public ResponseEntity<ApiResponse<List<SprintLeaderboardEntryDto>>> getMonthlyLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {

        List<SprintLeaderboardEntryDto> result = sprintLeaderboardService.getMonthlyTop(
                Math.min(limit, 100));
        return ResponseEntity.ok(ApiResponse.success("Monthly leaderboard fetched", result));
    }
}
