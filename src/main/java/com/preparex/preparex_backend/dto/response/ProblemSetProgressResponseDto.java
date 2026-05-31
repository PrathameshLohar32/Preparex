package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for problem set progress endpoint.
 * Returns total, completed, and percentage for the current user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSetProgressResponseDto {

    private Long total;
    private Long completed;
    private Double percentage;
}
