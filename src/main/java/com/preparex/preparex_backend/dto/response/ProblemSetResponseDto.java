package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for problem set listings and detail view.
 * Includes completion metrics when a user context is available.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSetResponseDto {

    private UUID id;
    private String slug;
    private String title;
    private String description;
    private String examId;
    private Boolean isPremium;
    private Integer displayOrder;

    /** Total number of problems in this set */
    private Long problemCount;

    /**
     * Number of problems completed by the current user.
     * Defaults to 0 until Phase 2 submissions are built.
     */
    @Builder.Default
    private Long completedCount = 0L;

    /**
     * Completion percentage for the current user.
     * Computed as (completedCount / problemCount) * 100.
     */
    @Builder.Default
    private Double percentage = 0.0;

    /**
     * Indicates whether the set is locked for the current user.
     * True if the set is premium and the user is a free-tier user.
     */
    @Builder.Default
    private Boolean locked = false;
}
