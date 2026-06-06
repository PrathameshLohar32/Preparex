package com.preparex.preparex_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for starting a new sprint session.
 * All filters are optional — null means no filter applied.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintStartRequestDto {

    /** Optional subject filter (e.g. "Physics"). Null = all subjects */
    private String subjectFilter;

    /** Optional difficulty filter (e.g. "MEDIUM"). Null = mixed difficulty */
    private String difficultyFilter;

    /** Optional exam filter (e.g. "JEE_MAINS"). Null = all exams */
    private String examId;
}
