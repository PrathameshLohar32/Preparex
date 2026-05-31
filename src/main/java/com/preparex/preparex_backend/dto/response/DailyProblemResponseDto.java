package com.preparex.preparex_backend.dto.response;

import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for a daily challenge problem.
 * Includes problem metadata and per-user completion status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyProblemResponseDto {

    private Integer dailyProblemId;
    private UUID problemId;
    private String title;
    private String slug;
    private String subjectName;
    private Difficulty difficulty;
    private QuestionType questionType;
    private Boolean isCompletedByUser;
}
