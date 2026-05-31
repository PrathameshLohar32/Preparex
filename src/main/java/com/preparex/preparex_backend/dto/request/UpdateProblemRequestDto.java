package com.preparex.preparex_backend.dto.request;

import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for updating an existing problem (admin use).
 * All fields are optional — only non-null fields are applied.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProblemRequestDto {

    private String title;

    private String bodyText;

    private String figureUrl;

    private QuestionType questionType;

    private Difficulty difficulty;

    private List<Map<String, Object>> options;

    private Map<String, Object> answerKey;

    private String solutionText;

    private List<String> hints;

    private Integer subjectId;

    private Integer topicId;

    private String examId;

    private Integer pyqYear;

    private Boolean isPremium;
}
