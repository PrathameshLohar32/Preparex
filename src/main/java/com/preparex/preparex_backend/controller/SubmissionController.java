package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.SubmissionFilterRequestDto;
import com.preparex.preparex_backend.dto.request.SubmitRequestDto;
import com.preparex.preparex_backend.dto.response.SubmissionHistoryResponseDto;
import com.preparex.preparex_backend.dto.response.SubmissionResponseDto;
import com.preparex.preparex_backend.dto.response.SubjectStatsResponseDto;
import com.preparex.preparex_backend.enums.SubmissionSource;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import com.preparex.preparex_backend.security.CustomUserDetails;
import com.preparex.preparex_backend.service.SubmissionService;
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

import java.util.UUID;

/**
 * Submission endpoints for authenticated users.
 * Handles problem submission scoring, history retrieval, and subject statistics.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Problem submission, history, and statistics APIs")
public class SubmissionController {

    private final SubmissionService submissionService;

    @Operation(
            summary = "Submit an answer",
            description = "Scores the answer using the strategy pattern. Returns result without answer_key."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<SubmissionResponseDto>> submit(
            @Valid @RequestBody SubmitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SubmissionResponseDto result = submissionService.submit(userDetails.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Submission scored successfully", result));
    }

    @Operation(
            summary = "Get submission history",
            description = "Returns paginated history with optional filters for problemId, status, and source."
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<SubmissionHistoryResponseDto>>> getHistory(
            @RequestParam(required = false) UUID problemId,
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(required = false) SubmissionSource source,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SubmissionFilterRequestDto filter = SubmissionFilterRequestDto.builder()
                .problemId(problemId)
                .status(status)
                .source(source)
                .page(page)
                .size(size)
                .build();

        Page<SubmissionHistoryResponseDto> result = submissionService
                .getHistory(userDetails.getUserId(), filter);

        return ResponseEntity.ok(ApiResponse.success("History fetched successfully", result));
    }

    @Operation(
            summary = "Get submission statistics",
            description = "Returns subject-wise, difficulty-wise solved counts."
    )
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SubjectStatsResponseDto>> getStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        SubjectStatsResponseDto result = submissionService.getStats(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Stats fetched successfully", result));
    }
}
