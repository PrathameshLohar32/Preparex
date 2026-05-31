package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.response.ProblemSetProgressResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemSetResponseDto;
import com.preparex.preparex_backend.security.CustomUserDetails;
import com.preparex.preparex_backend.service.PremiumAccessGuard;
import com.preparex.preparex_backend.service.ProblemSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public problem set endpoints for authenticated users.
 * Provides set listing, detail, and progress tracking.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/problem-sets")
@RequiredArgsConstructor
@Tag(name = "Problem Sets", description = "Curated problem set listing, detail, and progress APIs")
public class ProblemSetController {

    private final ProblemSetService problemSetService;
    private final PremiumAccessGuard premiumAccessGuard;

    @Operation(
            summary = "List all problem sets",
            description = "Returns all active problem sets. Premium sets show locked=true for free users."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProblemSetResponseDto>>> getAllSets(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        boolean isPremium = premiumAccessGuard.isPremiumUser(userDetails.getUserId());
        List<ProblemSetResponseDto> result = problemSetService.getAllSets(userDetails.getUserId(), isPremium);
        return ResponseEntity.ok(ApiResponse.success("Problem sets fetched successfully", result));
    }

    @Operation(
            summary = "Get problem set detail",
            description = "Returns set metadata with problems and completion metrics."
    )
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProblemSetResponseDto>> getSetBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ProblemSetResponseDto result = problemSetService.getSetBySlug(slug, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Problem set fetched successfully", result));
    }

    @Operation(
            summary = "Get user progress for a problem set",
            description = "Returns total, completed, and completion percentage for the current user."
    )
    @GetMapping("/{slug}/progress")
    public ResponseEntity<ApiResponse<ProblemSetProgressResponseDto>> getProgress(
            @PathVariable String slug,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ProblemSetProgressResponseDto result = problemSetService.getProgress(slug, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Progress fetched successfully", result));
    }
}
