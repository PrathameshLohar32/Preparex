package com.preparex.preparex_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for answering a question in an active sprint session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintAnswerRequestDto {

    @NotNull(message = "Problem ID is required")
    private UUID problemId;

    @NotNull(message = "Answer is required")
    private Map<String, Object> answer;

    /** Time the user spent on this question in seconds */
    private Integer timeTakenSecs;
}
