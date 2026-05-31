package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.AddProblemToSetRequestDto;
import com.preparex.preparex_backend.dto.request.CreateProblemRequestDto;
import com.preparex.preparex_backend.dto.request.CreateProblemSetRequestDto;
import com.preparex.preparex_backend.dto.request.UpdateProblemRequestDto;
import com.preparex.preparex_backend.dto.response.ProblemDetailResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemSetResponseDto;
import com.preparex.preparex_backend.service.ProblemService;
import com.preparex.preparex_backend.service.ProblemSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin-only endpoints for managing problems and problem sets.
 * All endpoints require ADMIN role authorization.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Problems", description = "Admin APIs for problem and problem set management")
public class AdminProblemController {

    private final ProblemService problemService;
    private final ProblemSetService problemSetService;

    // ── Problem Management ──────────────────────────────────────────────

    @Operation(
            summary = "Create a new problem",
            description = "Creates a problem with all fields including JSONB options and answer_key."
    )
    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<ProblemDetailResponseDto>> createProblem(
            @Valid @RequestBody CreateProblemRequestDto request) {

        ProblemDetailResponseDto result = problemService.createProblem(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Problem created successfully", result));
    }

    @Operation(
            summary = "Update an existing problem",
            description = "Updates non-null fields. Evicts Redis cache on success."
    )
    @PutMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<ProblemDetailResponseDto>> updateProblem(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProblemRequestDto request) {

        ProblemDetailResponseDto result = problemService.updateProblem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Problem updated successfully", result));
    }

    @Operation(
            summary = "Soft-delete a problem",
            description = "Sets is_active=false. Evicts Redis cache on success."
    )
    @DeleteMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable UUID id) {

        problemService.deleteProblem(id);
        return ResponseEntity.ok(ApiResponse.success("Problem deleted successfully"));
    }

    // ── Problem Set Management ──────────────────────────────────────────

    @Operation(
            summary = "Create a curated problem set",
            description = "Creates a new problem set with title, slug, description, and premium flag."
    )
    @PostMapping("/problem-sets")
    public ResponseEntity<ApiResponse<ProblemSetResponseDto>> createProblemSet(
            @Valid @RequestBody CreateProblemSetRequestDto request) {

        ProblemSetResponseDto result = problemSetService.createProblemSet(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Problem set created successfully", result));
    }

    @Operation(
            summary = "Add a problem to a set",
            description = "Adds a problem to an existing set at the specified position. Idempotent — duplicates return 409."
    )
    @PostMapping("/problem-sets/{id}/problems")
    public ResponseEntity<ApiResponse<Void>> addProblemToSet(
            @PathVariable UUID id,
            @Valid @RequestBody AddProblemToSetRequestDto request) {

        problemSetService.addProblemToSet(id, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Problem added to set successfully"));
    }
}
