package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin profile management endpoints.
 *
 * <p>Currently exposes a single endpoint:
 * <ul>
 *   <li>POST /api/v1/admin/profile/{userId}/recalculate — rebuild solved_stats and
 *       subject_stats from raw submissions. Used as a recovery mechanism when Kafka
 *       consumers miss events (e.g. after a replay or data migration).</li>
 * </ul>
 * </p>
 *
 * <p>All endpoints require the ADMIN role ({@code @PreAuthorize("hasRole('ADMIN')")}
 * at class level). Method Security must be enabled via
 * {@code @EnableMethodSecurity} (already set in SecurityConfig).</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Profile", description = "Admin profile management — stats recalculation and recovery")
public class AdminProfileController {

    private final ProfileService profileService;

    @Operation(
            summary = "Recalculate user stats from raw submissions",
            description = "Deletes the user's UserSolvedStats and UserSubjectStat rows then " +
                    "rebuilds them by replaying all raw Submission records. " +
                    "Use this as a recovery mechanism when Kafka consumers have missed events, " +
                    "or after a data migration. Evicts all Redis profile caches for this user. " +
                    "This is idempotent — calling it multiple times produces the same result."
    )
    @PostMapping("/{userId}/recalculate")
    public ResponseEntity<ApiResponse<Void>> recalculateStats(
            @Parameter(description = "UUID of the target user", required = true)
            @PathVariable UUID userId) {

        log.info("Admin triggered stats recalculation for userId={}", userId);
        profileService.recalculateStats(userId);
        return ResponseEntity.ok(ApiResponse.success(
                "Stats recalculated for user " + userId, null));
    }
}
