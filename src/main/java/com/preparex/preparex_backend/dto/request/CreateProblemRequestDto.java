package com.preparex.preparex_backend.dto.request;

import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a new problem (admin use).
 * Accepts all problem fields including JSONB data for options, answer_key, and hints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProblemRequestDto {

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body text is required")
    private String bodyText;

    private String figureUrl;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    /** JSONB options — null for NUMERICAL type */
    private List<Map<String, Object>> options;

    @NotNull(message = "Answer key is required")
    private Map<String, Object> answerKey;

    private String solutionText;

    /** Array of hint strings revealed one at a time */
    private List<String> hints;

    private Integer subjectId;

    private Integer topicId;

    @NotBlank(message = "Exam ID is required")
    private String examId;

    /** Previous year question year — null if not PYQ */
    private Integer pyqYear;

    /** Parent problem ID for PARAGRAPH-type child questions */
    private String parentId;

    @Builder.Default
    private Boolean isPremium = false;
}
