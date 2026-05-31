package com.preparex.preparex_backend.dto.response;

import com.preparex.preparex_backend.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after completing a daily challenge problem.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCompletionResponseDto {

    private Boolean correct;
    private SubmissionStatus status;
    private Integer marksAwarded;
    private String explanation;
    private Boolean alreadyCompleted;
}
