package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subject graph entry for radar chart visualization.
 * Shows per-subject accuracy for the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectGraphEntryDto {

    private String subject;
    private int solved;
    private double accuracy;
}
