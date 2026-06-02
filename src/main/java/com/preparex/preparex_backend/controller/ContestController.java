package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.ContestSubmitRequestDto;
import com.preparex.preparex_backend.dto.response.ContestResponseDto;
import com.preparex.preparex_backend.dto.response.ContestResultResponseDto;
import com.preparex.preparex_backend.dto.response.LeaderboardEntryDto;
import com.preparex.preparex_backend.enums.ContestStatus;
import com.preparex.preparex_backend.security.CustomUserDetails;
import com.preparex.preparex_backend.service.ContestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Student-facing contest endpoints.
 * All endpoints require authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/contests")
@RequiredArgsConstructor
@Tag(name = "Contests", description = "Student contest APIs — list, register, submit, leaderboard, results")
public class ContestController {

    private final ContestService contestService;

    @Operation(summary = "List contests", description = "Filter by status: upcoming|live|ended. Paginated.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ContestResponseDto>>> listContests(
            @RequestParam(required = false) ContestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<ContestResponseDto> result = contestService.listContests(
                status, page, size, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Contests fetched", result));
    }

    @Operation(summary = "Get contest detail")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContestResponseDto>> getContest(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ContestResponseDto result = contestService.getContest(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Contest fetched", result));
    }

    @Operation(summary = "Register for contest", description = "Idempotent. RLock prevents double-registration.")
    @PostMapping("/{id}/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        contestService.register(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Registered successfully", null));
    }

    @Operation(summary = "Get contest questions", description = "Only if LIVE + registered. No answer_key.")
    @GetMapping("/{id}/questions")
    public ResponseEntity<ApiResponse<List<Object>>> getQuestions(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<Object> questions = contestService.getQuestions(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Questions fetched", questions));
    }

    @Operation(summary = "Submit answer", description = "Async — returns 202 Accepted immediately. Kafka processes scoring.")
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Void>> submitAnswer(
            @PathVariable UUID id,
            @Valid @RequestBody ContestSubmitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        contestService.submitAnswer(id, userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Submission received — scoring in progress", null));
    }

    @Operation(summary = "Final submit", description = "Marks user done, locks further submissions.")
    @PatchMapping("/{id}/finalsubmit")
    public ResponseEntity<ApiResponse<Void>> finalSubmit(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        contestService.finalSubmit(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Final submission recorded", null));
    }

    @Operation(summary = "Get leaderboard", description = "Reads Redis ZSet. Returns top 50 + caller's rank.")
    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getLeaderboard(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<LeaderboardEntryDto> result = contestService.getLeaderboard(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Leaderboard fetched", result));
    }

    @Operation(summary = "Get my result", description = "Personal result after RESULTS_PUBLISHED.")
    @GetMapping("/{id}/results/me")
    public ResponseEntity<ApiResponse<ContestResultResponseDto>> getMyResult(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ContestResultResponseDto result = contestService.getMyResult(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Result fetched", result));
    }

    @Operation(summary = "Get all results", description = "Full leaderboard paginated — only after RESULTS_PUBLISHED.")
    @GetMapping("/{id}/results")
    public ResponseEntity<ApiResponse<Page<ContestResultResponseDto>>> getResults(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<ContestResultResponseDto> result = contestService.getResults(id, page, size);
        return ResponseEntity.ok(ApiResponse.success("Results fetched", result));
    }
}
