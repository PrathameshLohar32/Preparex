package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for passage-type questions.
 * Returns the parent problem and all its child questions in one response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassageResponseDto {

    private ProblemDetailResponseDto parent;
    private List<ProblemDetailResponseDto> children;
}
