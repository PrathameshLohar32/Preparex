package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.AddContestProblemRequestDto;
import com.preparex.preparex_backend.dto.request.CreateContestRequestDto;
import com.preparex.preparex_backend.dto.response.ContestResponseDto;
import com.preparex.preparex_backend.service.ContestService;
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
 * Admin contest management endpoints.
 * All endpoints require ADMIN role.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/contests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Contests", description = "Admin contest management — create, publish, add problems, force end")
public class AdminContestController {

    private final ContestService contestService;

    @Operation(summary = "Create contest", description = "Creates a new contest with status=DRAFT.")
    @PostMapping
    public ResponseEntity<ApiResponse<ContestResponseDto>> createContest(
            @Valid @RequestBody CreateContestRequestDto request) {

        ContestResponseDto result = contestService.createContest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contest created", result));
    }

    @Operation(summary = "Publish contest", description = "DRAFT → SCHEDULED. Validates problems and start time.")
    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<Void>> publishContest(@PathVariable UUID id) {
        contestService.publishContest(id);
        return ResponseEntity.ok(ApiResponse.success("Contest published", null));
    }

    @Operation(summary = "Add problem to contest", description = "Attach a problem with position, marks, negative marks.")
    @PostMapping("/{id}/problems")
    public ResponseEntity<ApiResponse<Void>> addProblem(
            @PathVariable UUID id,
            @Valid @RequestBody AddContestProblemRequestDto request) {

        contestService.addProblem(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Problem added to contest", null));
    }

    @Operation(summary = "Force end contest", description = "LIVE → ENDED. Publishes contest-ended Kafka event.")
    @PatchMapping("/{id}/end")
    public ResponseEntity<ApiResponse<Void>> forceEndContest(@PathVariable UUID id) {
        contestService.forceEndContest(id);
        return ResponseEntity.ok(ApiResponse.success("Contest force-ended", null));
    }
}
