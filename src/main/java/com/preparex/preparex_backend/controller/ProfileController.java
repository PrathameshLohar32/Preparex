package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.UpdateProfileRequestDto;
import com.preparex.preparex_backend.dto.response.*;
import com.preparex.preparex_backend.security.CustomUserDetails;
import com.preparex.preparex_backend.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * User profile and analytics REST controller.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /api/v1/profile/me            — full profile (Redis cached 15 min)</li>
 *   <li>GET  /api/v1/profile/{userId}       — public profile (no email/phone)</li>
 *   <li>PUT  /api/v1/profile/me            — update bio/social links/preferences</li>
 *   <li>GET  /api/v1/profile/me/heatmap    — 365-day activity heatmap</li>
 *   <li>GET  /api/v1/profile/me/contest-history — paginated contest history</li>
 *   <li>GET  /api/v1/profile/me/contest-graph   — score progression for line chart</li>
 *   <li>GET  /api/v1/profile/me/subject-graph   — per-subject accuracy for radar chart</li>
 *   <li>GET  /api/v1/profile/me/badges     — earned + locked badges</li>
 * </ul>
 * </p>
 *
 * <p>All endpoints require JWT authentication. The /profile/{userId} endpoint returns
 * only the public subset (bio, stats, streaks) — never email or phone.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile and analytics — stats, heatmap, badges, contest graph")
public class ProfileController {

    private final ProfileService profileService;

    // ── Own Profile ──────────────────────────────────────────────────────

    @Operation(
            summary = "Get my full profile",
            description = "Returns the authenticated user's complete profile: personal info, " +
                    "solved stats, streak, sprint stats, and recent badges. " +
                    "Response is Redis-cached for 15 minutes; evicted on PUT /profile/me."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserProfileResponseDto profile = profileService.getFullProfile(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", profile));
    }

    @Operation(
            summary = "Update my profile",
            description = "Partially updates the authenticated user's profile. " +
                    "Only non-null fields in the request body are applied. " +
                    "Evicts the full profile Redis cache on success."
    )
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserProfileResponseDto updated = profileService.updateProfile(userDetails.getUserId(), request);
        log.info("Profile updated for userId={}", userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
    }

    // ── Activity Heatmap ─────────────────────────────────────────────────

    @Operation(
            summary = "Get my activity heatmap",
            description = "Returns a list of {date, count} entries covering the last 365 days. " +
                    "Merges daily challenge completions and practice/contest submissions. " +
                    "Redis-cached for 1 hour."
    )
    @GetMapping("/me/heatmap")
    public ResponseEntity<ApiResponse<List<HeatmapEntryDto>>> getMyHeatmap(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<HeatmapEntryDto> heatmap = profileService.getHeatmap(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Heatmap fetched", heatmap));
    }

    // ── Contest Analytics ────────────────────────────────────────────────

    @Operation(
            summary = "Get my contest history",
            description = "Returns a paginated list of past contests with score, rank, percentile, " +
                    "and correct/wrong/unattempted counts. Sorted newest-first. " +
                    "Redis-cached per user for 30 minutes."
    )
    @GetMapping("/me/contest-history")
    public ResponseEntity<ApiResponse<Page<ContestHistoryDto>>> getMyContestHistory(
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int cappedSize = Math.min(size, 50);
        Page<ContestHistoryDto> history =
                profileService.getContestHistory(userDetails.getUserId(), page, cappedSize);
        return ResponseEntity.ok(ApiResponse.success("Contest history fetched", history));
    }

    @Operation(
            summary = "Get my contest score progression graph",
            description = "Returns [{title, score, rank, percentile, date}] sorted by date ascending. " +
                    "Use this to render a score-over-time line chart on the profile page."
    )
    @GetMapping("/me/contest-graph")
    public ResponseEntity<ApiResponse<List<ContestGraphEntryDto>>> getMyContestGraph(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ContestGraphEntryDto> graph = profileService.getContestGraph(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Contest graph fetched", graph));
    }

    // ── Subject Analytics ────────────────────────────────────────────────

    @Operation(
            summary = "Get my subject-wise performance graph",
            description = "Returns [{subject, solved, accuracy}] from user_subject_stats. " +
                    "Use this to render a radar/spider chart on the profile page. " +
                    "Accuracy is a 0–100 float rounded to 1 decimal."
    )
    @GetMapping("/me/subject-graph")
    public ResponseEntity<ApiResponse<List<SubjectGraphEntryDto>>> getMySubjectGraph(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<SubjectGraphEntryDto> graph = profileService.getSubjectGraph(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Subject graph fetched", graph));
    }

    // ── Badges ───────────────────────────────────────────────────────────

    @Operation(
            summary = "Get my badges",
            description = "Returns {earned: [...], locked: [...]}. " +
                    "Earned badges have badgeType, context and awardedAt. " +
                    "Locked badges are all BadgeType values minus the earned set. " +
                    "Redis-cached for 30 minutes; evicted by BadgeConsumer on new award."
    )
    @GetMapping("/me/badges")
    public ResponseEntity<ApiResponse<BadgesResponseDto>> getMyBadges(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        BadgesResponseDto badges = profileService.getBadges(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Badges fetched", badges));
    }

    // ── Public Profile ───────────────────────────────────────────────────

    @Operation(
            summary = "Get public profile of any user",
            description = "Returns a public subset of the user profile: name, username, bio, " +
                    "location, social links, total solved, streak info, badges earned count, " +
                    "and sprint stats. Never returns email or phone number."
    )
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<PublicProfileResponseDto>> getPublicProfile(
            @Parameter(description = "UUID of the target user", required = true)
            @PathVariable UUID userId) {

        PublicProfileResponseDto profile = profileService.getPublicProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Public profile fetched", profile));
    }
}
