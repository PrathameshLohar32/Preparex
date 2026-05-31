package com.preparex.preparex_backend.dto.response;

import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Lightweight problem representation for paginated listing.
 * NEVER includes answer_key.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemListItemResponseDto {

    private UUID id;
    private String slug;
    private String title;
    private Difficulty difficulty;
    private QuestionType questionType;
    private String topicName;
    private String subjectName;
    private Boolean isPremium;
    private Integer attemptCount;
    private String examId;
    private Integer pyqYear;

    /**
     * Whether the current user has solved this problem.
     * Defaults to false until Phase 2 submissions are built.
     */
    @Builder.Default
    private Boolean isSolved = false;
}
