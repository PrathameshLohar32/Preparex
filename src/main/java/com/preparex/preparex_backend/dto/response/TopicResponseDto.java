package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for topic listings.
 * Includes denormalized subject name for convenience.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponseDto {

    private Integer id;
    private String name;
    private Integer subjectId;
    private String subjectName;
    private Integer displayOrder;
}
