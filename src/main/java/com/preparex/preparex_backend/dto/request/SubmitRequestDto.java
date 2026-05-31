package com.preparex.preparex_backend.dto.request;

import com.preparex.preparex_backend.enums.SubmissionSource;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for submitting an answer to a problem.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitRequestDto {

    @NotNull(message = "Problem ID is required")
    private UUID problemId;

    @NotNull(message = "Answer is required")
    private Map<String, Object> answer;

    private Integer timeTakenSecs;

    @Builder.Default
    private SubmissionSource source = SubmissionSource.PRACTICE;
}
