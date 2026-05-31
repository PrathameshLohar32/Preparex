package com.preparex.preparex_backend.dto.request;

import com.preparex.preparex_backend.enums.SubmissionSource;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Filter parameters for submission history endpoint.
 * All fields are optional — null values are ignored.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionFilterRequestDto {

    private UUID problemId;
    private SubmissionStatus status;
    private SubmissionSource source;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}
