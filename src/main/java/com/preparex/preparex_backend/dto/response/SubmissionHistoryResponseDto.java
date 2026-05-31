package com.preparex.preparex_backend.dto.response;

import com.preparex.preparex_backend.enums.SubmissionSource;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for submission history items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionHistoryResponseDto {

    private UUID id;
    private UUID problemId;
    private String problemTitle;
    private String problemSlug;
    private SubmissionStatus status;
    private Integer marksAwarded;
    private Integer timeTakenSecs;
    private SubmissionSource source;
    private Instant submittedAt;
}
