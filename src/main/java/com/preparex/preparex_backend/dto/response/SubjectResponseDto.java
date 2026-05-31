package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for subject listings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponseDto {

    private Integer id;
    private String name;
    private String examId;
    private Integer displayOrder;
}
