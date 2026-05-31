package com.preparex.preparex_backend.dto.response;

import com.preparex.preparex_backend.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after scoring a submission.
 * NEVER includes answer_key.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponseDto {

    private Boolean correct;
    private SubmissionStatus status;
    private Integer marksAwarded;
    private String explanation;
}
