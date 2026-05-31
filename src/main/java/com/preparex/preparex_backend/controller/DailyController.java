package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.SubmitRequestDto;
import com.preparex.preparex_backend.dto.response.DailyCompletionResponseDto;
import com.preparex.preparex_backend.dto.response.DailyProblemResponseDto;
import com.preparex.preparex_backend.dto.response.UserStreakResponseDto;
import com.preparex.preparex_backend.security.CustomUserDetails;
import com.preparex.preparex_backend.service.DailyProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Daily challenge endpoints for authenticated users.
 * Handles today's problems, completion, calendar, and streak.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/daily")
@RequiredArgsConstructor
@Tag(name = "Daily Challenge", description = "Daily problem, completion, calendar, and streak APIs")
public class DailyController {

    private final DailyProblemService dailyProblemService;

    @Operation(
            summary = "Get today's daily problems",
            description = "Returns 3 problems (one per subject). Redis cached until midnight."
    )
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<DailyProblemResponseDto>>> getToday(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<DailyProblemResponseDto> result = dailyProblemService.getToday(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Daily problems fetched successfully", result));
    }

    @Operation(
            summary = "Complete a daily problem",
            description = "Submits answer and marks daily complete. Idempotent — duplicate returns existing."
    )
    @PostMapping("/{dailyProblemId}/complete")
    public ResponseEntity<ApiResponse<DailyCompletionResponseDto>> complete(
            @PathVariable Integer dailyProblemId,
            @Valid @RequestBody SubmitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DailyCompletionResponseDto result = dailyProblemService
                .complete(userDetails.getUserId(), dailyProblemId, request);

        HttpStatus status = result.getAlreadyCompleted() ? HttpStatus.OK : HttpStatus.CREATED;
        String message = result.getAlreadyCompleted()
                ? "Daily problem already completed"
                : "Daily problem completed successfully";

        return ResponseEntity
                .status(status)
                .body(ApiResponse.success(message, result));
    }

    @Operation(
            summary = "Get 90-day calendar",
            description = "Returns {date: 'SOLVED'|'MISSED'|'FUTURE'} map for last 90 days."
    )
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<Map<LocalDate, String>>> getCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Map<LocalDate, String> result = dailyProblemService.getCalendar(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Calendar fetched successfully", result));
    }

    @Operation(
            summary = "Get user streak",
            description = "Returns {currentStreak, longestStreak, lastActiveDate}. Redis cached 30min."
    )
    @GetMapping("/streak")
    public ResponseEntity<ApiResponse<UserStreakResponseDto>> getStreak(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserStreakResponseDto result = dailyProblemService.getStreak(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Streak fetched successfully", result));
    }
}
